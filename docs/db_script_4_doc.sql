drop table if exists person CASCADE;
drop table if exists service_provided CASCADE;
drop table if exists service_registration CASCADE;
drop table if exists service_schedule CASCADE;

CREATE TABLE IF NOT EXISTS public.person
(
    telegram_id bigint NOT NULL,
    fio character varying(255) NOT NULL,
    phone character varying(255),
    role character varying(10) NOT NULL DEFAULT 'CLIENT',
    first_name character varying(255),
    CONSTRAINT person_id PRIMARY KEY (telegram_id)
);

COMMENT ON TABLE public.person IS 'Пользователи';
COMMENT ON COLUMN public.person.telegram_id IS 'User ID из телеграм';
COMMENT ON COLUMN public.person.fio IS 'ФИО пользователя';
COMMENT ON COLUMN public.person.phone IS 'Номер(а) телефона(ов)';
COMMENT ON COLUMN public.person.role IS 'Роль юзера (CLIENT/ADMIN)';
COMMENT ON COLUMN public.person.first_name IS 'Значение firstName из сообщений Телеграма';

CREATE TABLE IF NOT EXISTS public.service_provided
(
    service_id bigint generated by default as identity,
    name character varying(255) NOT NULL,
    duration integer NOT NULL DEFAULT 60,
    capacity integer NOT NULL DEFAULT 1,
    price bigint NOT NULL DEFAULT 0,
    executor_telegram_id bigint NOT NULL,
    CONSTRAINT service_pk PRIMARY KEY (service_id),
    CONSTRAINT service_name_un UNIQUE (name, executor_telegram_id)
);

COMMENT ON TABLE public.service_provided IS 'Список предоставляемых услуг';
COMMENT ON COLUMN public.service_provided.service_id IS 'Автогенерируемый идентификатор';
COMMENT ON COLUMN public.service_provided.name IS 'Наименование услуги';
COMMENT ON COLUMN public.service_provided.duration IS 'Длительность услуги в минутах';
COMMENT ON COLUMN public.service_provided.capacity IS 'Кол-во клиентов на сеанс услуги';
COMMENT ON COLUMN public.service_provided.price IS 'Стоимость услуги в копейках.';
COMMENT ON COLUMN public.service_provided.executor_telegram_id IS 'telegram_id исполнителя услуги';

CREATE TABLE IF NOT EXISTS public.service_schedule
(
    schedule_id bigint generated by default as identity,
    ins_date timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    customer_telegram_id bigint NOT NULL,
    service_id bigint NOT NULL,
    service_date date NOT NULL,
    service_time time NOT NULL,
    is_visited boolean NOT NULL DEFAULT false,
    CONSTRAINT schedule_pk PRIMARY KEY (schedule_id),
    CONSTRAINT schedule_uniq UNIQUE (customer_telegram_id, service_id, service_date, service_time)
);

COMMENT ON TABLE public.service_schedule IS 'Расписание услуг';
COMMENT ON COLUMN public.service_schedule.schedule_id IS 'Автогенерируемый идентификатор';
COMMENT ON COLUMN public.service_schedule.ins_date IS 'Дата и время вставки записи - с таймзоной для достоверности';
COMMENT ON COLUMN public.service_schedule.customer_telegram_id IS 'telegram_id заказчика услуги';
COMMENT ON COLUMN public.service_schedule.service_id IS 'Запрошенная услуга';
COMMENT ON COLUMN public.service_schedule.service_date IS 'Дата запрошенной услуги';
COMMENT ON COLUMN public.service_schedule.service_time IS 'Время запрошенной услуги';
COMMENT ON COLUMN public.service_schedule.is_visited IS 'Услуга оказана (true/false)';

ALTER TABLE IF EXISTS public.service_provided
    ADD FOREIGN KEY (executor_telegram_id)
        REFERENCES public.person (telegram_id)
        ON DELETE restrict;

ALTER TABLE IF EXISTS public.service_schedule
    ADD FOREIGN KEY (customer_telegram_id)
        REFERENCES public.person (telegram_id)
        ON DELETE restrict;

ALTER TABLE IF EXISTS public.service_schedule
    ADD FOREIGN KEY (service_id)
        REFERENCES public.service_provided (service_id)
        ON DELETE restrict;

insert into PERSON (TELEGRAM_ID, FIO, PHONE, ROLE, FIRST_NAME)
values (920478661, 'Иванов Андрей', '+79296268859', 'ADMIN', 'Sir Hanry');

insert into SERVICE_PROVIDED (NAME, DURATION, CAPACITY, PRICE, executor_telegram_id)
values ('Стрижка', 60, 1, 1000, 920478663),
       ('Маникюр', 60, 1, 1500, 920478663),
       ('Солярий', 60, 2, 500, 920478663),
       ('Мойка авто', 60, 1, 700, 920478663);

insert into service_schedule (customer_telegram_id, SERVICE_ID, SERVICE_DATE, SERVICE_TIME)
values (920478661, 1, date '2023-02-13', TIME '08:00:00'),
       (920478661, 4, date '2023-02-13', TIME '09:00:00');

--список часов в формате: “${HH-MM}_${FIO},${serviceName}, ${price}
-- 10-00 Семенов Семен, стрижка, цена
-- 11-00 Игорев Игорь, груминг, цена

select ss.service_time, p.fio , sp.name, sp.price
from service_schedule ss
    inner join service_provided sp on ss.service_id = sp.service_id
    inner join person p on p.telegram_id = ss.customer_telegram_id
where ss.service_date = date '2023-02-13'
order by ss.service_time;