from flask import Flask, request, jsonify
from threading import Timer, Lock
import logging
from datetime import datetime, timedelta

app = Flask(__name__)

app.config['MAX_CONTENT_LENGTH'] = 1024 * 1024  # 1 MB

# Настройка логгера
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(message)s')
logger = logging.getLogger(__name__)

# Хранилище будильников
alarms = {}
alarms_lock = Lock()


class Alarm:
    def __init__(self, alarm_id, time, name="default"):
        self.alarm_id = alarm_id
        self.time = time
        self.name = name
        self.timer = None

    def schedule(self):
        delay = (self.time - datetime.now()).total_seconds()
        if delay > 0:
            self.timer = Timer(delay, self.trigger)
            self.timer.start()
            logger.info(f"Будильник создан: ID={self.alarm_id}, Время={self.time}, Имя={self.name}")
        else:
            logger.warning(f"Будильник {self.alarm_id} установлен на прошедшее время.")

    def cancel(self):
        if self.timer:
            self.timer.cancel()
            logger.info(f"Будильник отменен: ID={self.alarm_id}")

    def trigger(self):
        logger.info(f"Будильник сработал! ID={self.alarm_id}, Имя={self.name}")
        with alarms_lock:
            alarms.pop(self.alarm_id, None)
        logger.info(f"Будильник удален: ID={self.alarm_id}")


def validate_request_data(data):
    if not data:
        return jsonify({"error": "Пустой запрос."}), 400

    if len(str(data)) > 1000:
        return jsonify({"error": "Размер запроса превышает допустимый предел (1000 символов)."}), 400

    try:
        alarm_id = int(data.get('id'))
        time_hours = int(data.get('hours'))
        time_minutes = int(data.get('minutes'))
        name = data.get('name', "default")
    except (TypeError, ValueError):
        return jsonify({"error": "Некорректный формат данных. Проверьте 'id', 'hours' и 'minutes'."}), 400

    return alarm_id, time_hours, time_minutes, name


@app.route('/create_alarm', methods=['POST'])
def create_alarm():
    try:
        #force=True: Позволяет парсить данные даже если Content-Type не установлен как application/json.
        #silent=True: Не выбрасывает исключение, если JSON некорректный, вместо этого возвращает None.
        data = request.get_json(force=True, silent=True)

        if data is None:
            return jsonify({"error": "Тело запроса должно быть корректным JSON."}), 400
        
        if not isinstance(data, dict):
            return jsonify({"error": "JSON должен быть объектом (словарем)."}), 400

        # Проверка данных
        validate_response = validate_request_data(data)
        if isinstance(validate_response, tuple) and len(validate_response) == 4:
            alarm_id, time_hours, time_minutes, name = validate_response
        else:
            # Если возвращается ошибка из validate_request_data
            return validate_response

        alarm_id, time_hours, time_minutes, name = validate_response

        alarm_time = datetime.now().replace(hour=time_hours, minute=time_minutes, second=0, microsecond=0)
        if alarm_time < datetime.now():
            alarm_time += timedelta(days=1)

        with alarms_lock:
            for existing_alarm in alarms.values():
                if existing_alarm.time == alarm_time:
                    return jsonify({"error": "Будильник на это время уже существует."}), 400

            if alarm_id in alarms:
                return jsonify({"error": f"Будильник с ID {alarm_id} уже существует."}), 400

            new_alarm = Alarm(alarm_id, alarm_time, name)
            alarms[alarm_id] = new_alarm
            new_alarm.schedule()

        return jsonify({"message": f"Будильник {alarm_id} создан."}), 201
    
    except Exception as e:
        # Обработка непредвиденных исключений
        return jsonify({"error": "Ошибка обработки запроса.", "details": str(e)}), 500


@app.route('/delete_alarm/<alarm_id>', methods=['DELETE'])
def delete_alarm(alarm_id):
    # Проверка, что alarm_id - число (дополнительная защита)
    if not alarm_id.isdigit():
        return jsonify({"error": "ID будильника должен быть числом."}), 400
    
    alarm_id = int(alarm_id)

    # Проверка на корректность диапазона ID
    if alarm_id < 0:
        return jsonify({"error": "ID будильника не может быть отрицательным."}), 400

    with alarms_lock:
        alarm = alarms.pop(alarm_id, None)
        if alarm:
            alarm.cancel()
            return jsonify({"message": f"Будильник {alarm_id} отменен."}), 200
        else:
            return jsonify({"error": f"Будильник {alarm_id} не найден."}), 404


@app.route('/get_alarms', methods=['GET'])
def get_alarms():
    try:
        with alarms_lock:
            active_alarm_ids = list(alarms.keys())
        return jsonify({"alarms": active_alarm_ids}), 200

    except Exception as e:
        return jsonify({"error": "Ошибка при получении будильников.", "details": str(e)}), 500


@app.route('/update_alarm', methods=['PUT'])
def update_alarm():
    try:
        #force=True: Позволяет парсить данные даже если Content-Type не установлен как application/json.
        #silent=True: Не выбрасывает исключение, если JSON некорректный, вместо этого возвращает None.
        data = request.get_json(force=True, silent=True)

        if data is None:
            return jsonify({"error": "Тело запроса должно быть корректным JSON."}), 400
        
        if not isinstance(data, dict):
            return jsonify({"error": "JSON должен быть объектом (словарем)."}), 400
        
        # Проверка данных
        validate_response = validate_request_data(data)
        if isinstance(validate_response, tuple) and len(validate_response) == 4:
            alarm_id, time_hours, time_minutes, name = validate_response
        else:
            # Если возвращается ошибка из validate_request_data
            return validate_response

        alarm_id, time_hours, time_minutes, name = validate_response

        alarm_time = datetime.now().replace(hour=time_hours, minute=time_minutes, second=0, microsecond=0)
        if alarm_time < datetime.now():
            alarm_time += timedelta(days=1)

        with alarms_lock:
            alarm = alarms.get(alarm_id)
            if alarm:
                alarm.cancel()

            updated_alarm = Alarm(alarm_id, alarm_time, name)
            alarms[alarm_id] = updated_alarm
            updated_alarm.schedule()

        logger.info(f"Будильник обновлен: ID={alarm_id}, Новое время={alarm_time}, Новое имя={name}")
        return jsonify({"message": f"Будильник {alarm_id} обновлен."}), 200
    
    except Exception as e:
        # Обработка непредвиденных исключений
        return jsonify({"error": "Ошибка обработки запроса.", "details": str(e)}), 500


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
