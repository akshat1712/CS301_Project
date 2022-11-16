create or replace function insert_service_extra(train_no VARCHAR(5), station_code VARCHAR(5), arrival_time interval, station_name VARCHAR(50), departure_time interval, day_num integer)
        returns varchar
        language plpgsql
        as $$
        begin
        EXECUTE '
            INSERT INTO service_extra VALUES('''||train_no||''','''||station_code||''','''||arrival_time||''', '''||station_name||''','''||departure_time||''', '''||day_num||''');
        ';
        return 'success';
        end; $$ 
        ;