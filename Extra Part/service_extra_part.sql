-- CREATE TABLE valid_hops (
--     train_1 VARCHAR(5) NOT NULL,
--     train_2 VARCHAR(5) NOT NULL,
--     common_point VARCHAR(50) NOT NULL,
--     departure_time  interval NOT NULL,
--     waiting_time interval NOT NULL
-- );

-- CREATE TABLE IF NOT EXISTS service_extra(
--     train_no VARCHAR(5) ,
--     station_code VARCHAR(5),
--     arrival_time interval,
--     station_name VARCHAR(50) NOT NULL,
--     departure_time interval,
--     day_num int
-- );

-- CREATE TABLE all_routes(
--     start_station VARCHAR(50) NOT NULL,
--     train_1 VARCHAR(5) NOT NULL,
--     departure_time interval NOT NULL,
--     hop_station VARCHAR(50),
--     train_2 VARCHAR(5),
--     hop_time interval,
--     end_station VARCHAR(50) NOT NULL,
--     arrival_time interval NOT NULL
-- );

/*
see all possible departures from 'start'
1. same train also ends up at 'end'
2. else, another train
    > check if there is a valid meet between the 2 trains
    > store this in another table: <train_1> <train_2> <common_point>
For all stations, find and store if two trains meet within a valid interval
*/

-- select find_routes('KALKA', 'BELLARYJN');
-- select find_routes('KALKA', 'CHANDIGARH');


CREATE OR REPLACE FUNCTION find_valid_hops ()
returns VARCHAR
language plpgsql
as
$$
    declare
        all_stations_row record;
        train_1 record;
        train_2 record;
    begin
        EXECUTE 'DROP TABLE IF EXISTS valid_hops;';
        EXECUTE '
                   CREATE TABLE valid_hops (
                        train_1 VARCHAR(5) NOT NULL,
                        train_2 VARCHAR(5) NOT NULL,
                        common_point VARCHAR(50) NOT NULL,
                        departure_time  interval NOT NULL,
                        waiting_time interval NOT NULL
                    ); 
                ';
        for all_stations_row in (select distinct station_name 
                                from service_extra)
        loop
            for train_1 in select * from service_extra
            where station_name = all_stations_row.station_name
            loop
                for train_2 in select * from service_extra
                where station_name = all_stations_row.station_name
                loop
                    if train_1.train_no < train_2.train_no and
                        (train_2.departure_time - train_1.arrival_time between '-24:00' and '-12:00' or
                        train_2.departure_time - train_1.arrival_time between '0:00' and '12:00') then
                        INSERT INTO valid_hops VALUES(train_1.train_no, train_2.train_no, all_stations_row.station_name, train_2.departure_time, train_2.departure_time - train_1.arrival_time);
                    end if;
                end loop;
            end loop;
        end loop;
    return 'Valid hops computed';
    end;
$$;

CREATE OR REPLACE FUNCTION find_routes (start_station VARCHAR(50), end_station VARCHAR(50))
returns VARCHAR
language plpgsql
as
$$
    declare
        train_1 record;
        train_2 record;
        hop_routes_row record;
    begin
        EXECUTE '
                    DROP TABLE IF EXISTS all_routes;
                ';
        EXECUTE '
                    CREATE TABLE all_routes(
                        start_station VARCHAR(50) NOT NULL,
                        train_1 VARCHAR(5) NOT NULL,
                        departure_time interval NOT NULL,
                        hop_station VARCHAR(50),
                        train_2 VARCHAR(5),
                        hop_time interval,
                        end_station VARCHAR(50) NOT NULL,
                        arrival_time interval NOT NULL
                    );
                ';
        for train_1 in select * from service_extra
        where station_name = start_station
        loop
            for train_2 in select * from service_extra
            where station_name = end_station
            loop
                if train_1.train_no = train_2.train_no then
                    INSERT INTO all_routes VALUES(start_station, train_1.train_no, train_1.departure_time, NULL, NULL, NULL, end_station, train_2.arrival_time);
                else
                    for hop_routes_row in (
                        select * from valid_hops v
                        where v.train_1 = train_1.train_no and
                        v.train_2 = train_2.train_no and 
                        v.common_point <> start_station and
                        v.common_point <> end_station and
                        v.departure_time < train_2.arrival_time
                    )
                    loop
                        INSERT INTO all_routes VALUES(start_station, train_1.train_no, train_1.departure_time, hop_routes_row.common_point, train_2.train_no, hop_routes_row.departure_time, end_station, train_2.arrival_time);
                    end loop;
                end if;
            end loop;
        end loop;
        EXECUTE 'SELECT * FROM all_routes;';
        return 'All routes printed';
    end;
$$;