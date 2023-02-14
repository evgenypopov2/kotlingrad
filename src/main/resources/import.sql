insert into PERSON (TELEGRAM_ID, FIO, PHONE, ROLE, FIRST_NAME) values (920478663, 'Иванов Андрей', '+79296268859', 'ADMIN', 'Sir Hanry'), (920478661, 'Шлеменко Андрей', '+79296269259', 'ADMIN', 'Big Fighter'), (920478621, 'Кучеров Илья', '+79296269260', 'CLIENT', 'Small Fighter');

insert into SERVICE_PROVIDED (NAME, DURATION, CAPACITY, PRICE, executor_telegram_id) values ('Стрижка', 60, 1, 100000, 920478663),('Маникюр', 60, 1, 150000, 920478663),('Солярий', 60, 2, 50000, 920478663),('Мойка авто', 60, 1, 70000, 920478663),('Ремонт двигателя', 60, 1, 370000, 920478661);

insert into service_schedule (customer_telegram_id, SERVICE_ID, SERVICE_DATE, SERVICE_TIME) values (920478663, 1, date '2023-02-13', TIME '08:00:00'), (920478663, 4, date '2023-02-13', TIME '09:00:00'), (920478621, 5, date '2023-02-13', TIME '12:00:00');
