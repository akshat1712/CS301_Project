from multiprocessing.connection import wait
import psycopg2

conn = psycopg2.connect(
    host="localhost",
    database="project",
    user="cs301_pro",
    password="1234")

cur=conn.cursor()
commands =[
    """
     CREATE TABLE IF NOT EXISTS Stations(
        station_id VARCHAR(5) PRIMARY KEY,
        station_name VARCHAR(50) NOT NULL
        )
    """,
    """
       CREATE TABLE IF NOT EXISTS Trains(
        train_no VARCHAR(5) PRIMARY KEY,
        from_station  VARCHAR(5) NOT NULL,
        to_station  VARCHAR(5) NOT NULL,
        FOREIGN KEY(from_station) REFERENCES Stations(station_id),
        FOREIGN KEY(to_station) REFERENCES Stations(station_id)
        ) 
    """,
    """
        CREATE OR REPLACE FUNCTION NEW_TRAIN_TABLE()
        RETURNS TRIGGER
        LANGUAGE plpgsql
        AS
        $$
            DECLARE
            BEGIN
                    EXECUTE '
                        CREATE TABLE IF NOT EXISTS service_'||new.train_no||' (
                            travel_date DATE PRIMARY KEY NOT NULL,
                            ac_coach INTEGER NOT NULL,
                            sleeper_coach INTEGER NOT NULL,
                            ac_curr_berth INTEGER NOT NULL,
                            sleeper_curr_berth INTEGER NOT NULL
                        );		
                    ';
                    
                    EXECUTE '
                        CREATE TABLE IF NOT EXISTS ticket_'||new.train_no||' (
                            PNR varchar(20) PRIMARY KEY NOT NULL,
                            coach varchar(2) NOT NULL,
                            travel_date DATE NOT NULL,
                            train_no VARCHAR(5) NOT NULL,
                            FOREIGN KEY(travel_date) REFERENCES service_'||new.train_no||'(travel_date)
                        );		
                    ';

                    EXECUTE '
                        CREATE TABLE IF NOT EXISTS ticket_passenger_'||new.train_no||' (
                            name varchar(50) NOT NULL,
                            coach_no varchar(3) NOT NULL,
                            berth_no INTEGER NOT NULL,
                            berth_type char(2) NOT NULL,
                            PNR varchar(20) NOT NULL,
                            FOREIGN KEY(PNR) REFERENCES ticket_'||new.train_no||'(PNR)
                        );
                    ';
                RETURN NEW;
            END
        $$;
        CREATE OR REPLACE TRIGGER CREATE_TRAIN
        AFTER INSERT ON TRAINS
        FOR EACH ROW EXECUTE PROCEDURE NEW_TRAIN_TABLE();
    """,
    """
    create or replace function insert_ticket (
        num_pass int,
        train_no varchar,
        coach_type varchar,
        date_travel DATE,
        names varchar array
        ) 
        returns varchar
        language plpgsql
        as $$
        declare
        current int; 
        total int; 
        num_seats int;
        seat varchar(2);
        pnr varchar(20);
        c_train varchar(5);
        begin
        EXECUTE '
                    select train_no from Trains 
                    where train_no = '''||train_no ||''';	
            ' INTO c_train;
        if c_train IS NULL then
            raise exception '100E';
            return NULL;
        end if;
        
        if coach_type = 'AC' then
            num_seats := 18;
            EXECUTE '
                    select ac_curr_berth,ac_coach from service_'||train_no||' 
                    where travel_date = '' '||date_travel ||' '' FOR UPDATE;	
            ' INTO current,total; 
            if current IS NULL then
                raise exception '200E';
                return NULL;
            elsif current + num_pass> num_seats*total then
                raise exception '300E';
                return NULL;  
            else
                pnr := TO_CHAR(date_travel, 'MMDDYY') ||'0'||train_no|| current ;
                EXECUTE '
                            update  service_'||train_no||' set ac_curr_berth = '||current+num_pass||'
                            where travel_date = '' '||date_travel ||' '';	
                        ';   
                EXECUTE '
                            insert into ticket_'||train_no||' 
                            values('''||pnr|| ''','''||coach_type|| ''','''||date_travel|| ''','''||train_no||''');	
                        ';
                for cnt in 1..num_pass loop
                    select AC_berth(current) into seat;
                    EXECUTE '
                                insert into ticket_passenger_'||train_no||' 
                                values('''||names[cnt]|| ''','''||floor(current/num_seats)+1|| ''','''||mod(current,num_seats)+1|| ''','''||seat|| ''','''||pnr||''');	
                            ';
                    current := current + 1;
                end loop;
                return pnr;
            end if;
            
        elsif coach_type = 'SL' then 
            num_seats := 24;
            EXECUTE '
                        select sleeper_curr_berth,sleeper_coach from service_'||train_no||' 
                        where travel_date = '' '||date_travel ||' '' FOR UPDATE;	
            ' INTO current,total;
            if current IS NULL then
                raise exception '200E';
                return NULL;
            elsif current + num_pass> num_seats*total then
                raise exception '300E';
                return NULL;
            else
                pnr := TO_CHAR(date_travel, 'MMDDYY') ||'1'||train_no|| current ;
                EXECUTE '
                            update  service_'||train_no||' set sleeper_curr_berth = '||current+num_pass||'
                            where travel_date = '' '||date_travel ||' '';	
                        '; 
                EXECUTE '
                                    insert into ticket_'||train_no||' 
                                    values('''||pnr|| ''','''||coach_type|| ''','''||date_travel|| ''','''||train_no||''');	
                                ';
                for cnt in 1..num_pass loop
                    select SL_berth(current) into seat;
                    EXECUTE '
                                    insert into ticket_passenger_'||train_no||' 
                                    values('''||names[cnt]|| ''','''||floor(current/num_seats)+1|| ''','''||mod(current,num_seats)+1|| ''','''||seat|| ''','''||pnr||''');	
                                ';
                    current := current + 1;
                end loop;
            return pnr;
            end if;
        else
            return NULL;
        end if;
        end; $$ 
        ;
    """,
    """
    create or replace function AC_berth(berth_no int)
        returns varchar
        language plpgsql
        as $$
        declare
        current varchar(2);
        begin
            if berth_no%6 = 0 or berth_no%6 = 1 then
                current := 'LB';
            elsif berth_no%6 = 2 or berth_no%6 = 3 then
                current := 'UB';
            elsif berth_no%6 = 4 then
                current := 'SL';
            else
                current := 'SU';
            end if;
            return current;

        end; $$ 
        ;
    """,
    """
    create or replace function SL_berth(berth_no int)
        returns varchar
        language plpgsql
        as $$
        declare
        current varchar(2);
        begin
            if berth_no%8 = 0 or berth_no%8 = 3 then
                current := 'LB';
            elsif berth_no%8 = 1 or berth_no%8 = 4 then
                current := 'MB';
            elsif berth_no%8 = 2 or berth_no%8 = 5 then
                current := 'UB';
            elsif berth_no%8 = 6 then
                current := 'SL';
            else
                current := 'SU';
            end if;
            return current;

        end; $$ 
        ;
    """,
    """
    create or replace function insert_service_train(train_no VARCHAR(5),date_travel DATE,ac_coach INT,sleeper_coach INT)
        returns varchar
        language plpgsql
        as $$
        declare
        c_train varchar(5);
        from_train varchar := '12345';
        to_train varchar := '67890';
        begin
        EXECUTE '
                    select train_no from Trains
                    where train_no = '''||train_no ||''';
        '   INTO c_train;
        if c_train IS NULL then
            EXECUTE '
                INSERT INTO trains VALUES('''||train_no||''','''||from_train||''','''||to_train||''');
            ';
        end if;

        EXECUTE '
            INSERT INTO service_'||train_no||' VALUES('''||date_travel||''','''||ac_coach||''','''||sleeper_coach||''','''||0||''','''||0||''');
        ';

        return 'success';
        end; $$ 
        ;
    """,
    """
    CREATE TABLE IF NOT EXISTS service(
        train_no VARCHAR(5) ,
        station_departure VARCHAR(5) NOT NULL,
        station_arrival VARCHAR(5) NOT NULL,
        departure_time time,
        departure_day int,
        arrival_time time,
        arrival_day int
    );

    select dept.station_arrival, dept.departure_time, dept.arrival_time, arrv.departure_time , arrv.arrival_time from (select * from service where station_departure = 'abcde') dept,
    (select * from service where station_arrival = 'asdfg') arrv
    where dept.station_arrival = arrv.station_departure 
    and ((dept.arrival_time + interval '1 hour') < arrv.departure_time) and ((dept.arrival_time + interval '3 hour') > arrv.departure_time) ;
    """
]
import time
for i in commands:
    try:
        cur.execute(i)
    except (Exception, psycopg2.DatabaseError) as error:
        print(error)
        time.sleep(2)
cur.close()
conn.commit()