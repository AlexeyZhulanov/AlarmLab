from flask import Flask, request, jsonify
from threading import Timer, Lock
import logging
from datetime import datetime, timedelta

app = Flask(__name__)

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
        # Удаляем будильник из списка
        with alarms_lock:
            alarms.pop(self.alarm_id, None)
        logger.info(f"Будильник удален: ID={self.alarm_id}")


@app.route('/create_alarm', methods=['POST'])
def create_alarm():
    try:
        data = request.json
        alarm_id = int(data.get('id'))
        time_hours = int(data.get('hours'))
        time_minutes = int(data.get('minutes'))
        name = data.get('name', "default")

        if not alarm_id:
            raise ValueError("Поле 'id' обязательно.")

        # Определение времени будильника
        alarm_time = datetime.now().replace(hour=time_hours, minute=time_minutes, second=0, microsecond=0)
        if alarm_time < datetime.now():
            alarm_time += timedelta(days=1)

        with alarms_lock:
            # Проверка на уникальность времени
            for existing_alarm in alarms.values():
                if existing_alarm.time == alarm_time:
                    raise ValueError("Будильник на это время уже существует.")

            if alarm_id in alarms:
                raise ValueError(f"Будильник с ID {alarm_id} уже существует.")
            
            # Создаем новый будильник
            new_alarm = Alarm(alarm_id, alarm_time, name)
            alarms[alarm_id] = new_alarm
            new_alarm.schedule()

        return jsonify({"message": f"Будильник {alarm_id} создан."}), 201
    except Exception as e:
        logger.error(f"Ошибка создания будильника: {str(e)}")
        return jsonify({"error": str(e)}), 400


@app.route('/delete_alarm/<int:alarm_id>', methods=['DELETE'])
def delete_alarm(alarm_id):
    with alarms_lock:
        alarm = alarms.pop(alarm_id, None)
        if alarm:
            alarm.cancel()
            return jsonify({"message": f"Будильник {alarm_id} отменен."}), 200
        else:
            return jsonify({"error": f"Будильник {alarm_id} не найден."}), 404


@app.route('/get_alarms', methods=['GET'])
def get_alarms():
    with alarms_lock:
        active_alarm_ids = list(alarms.keys())
    return jsonify({"alarms": active_alarm_ids}), 200


@app.route('/update_alarm', methods=['PUT'])
def update_alarm():
    try:
        data = request.json
        alarm_id = int(data.get('id'))
        time_hours = int(data.get('hours'))
        time_minutes = int(data.get('minutes'))
        name = data.get('name', "default")

        if not alarm_id:
            raise ValueError("Поле 'id' обязательно.")

        # Определение нового времени будильника
        alarm_time = datetime.now().replace(hour=time_hours, minute=time_minutes, second=0, microsecond=0)
        if alarm_time < datetime.now():
            alarm_time += timedelta(days=1)

        with alarms_lock:
            alarm = alarms.get(alarm_id)
            if alarm:
                # Удаляем старый таймер
                alarm.cancel()

            # Создаем и заменяем будильник
            updated_alarm = Alarm(alarm_id, alarm_time, name)
            alarms[alarm_id] = updated_alarm
            updated_alarm.schedule()

        logger.info(f"Будильник обновлен: ID={alarm_id}, Новое время={alarm_time}, Новое имя={name}")
        return jsonify({"message": f"Будильник {alarm_id} обновлен."}), 200
    except Exception as e:
        logger.error(f"Ошибка обновления будильника: {str(e)}")
        return jsonify({"error": str(e)}), 400


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
