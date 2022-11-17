EXTRA TEAM MEMBER PART: SEARCH FOR TRAINS 
> We used the real-life train data from IRCTC for the following trains: "12012", "12046", "12058", "12650", "12992", "19412", "22692" 
> We have accounted for the difference in day for the same train. 
> Assumption: All trains run everyday.

Hops:
> Any hop that involves <= 12 hours of waiting time is considered a valid hop. This number was chosen to accomodate more routes.
> All valid hops are stored in a table called 'valid_hops'

Output:
> Following is the format of the output: 
start_station, train_1, departure_time, hop_station, train_2, hop_time, end_station, arrival_time, approx_days
> approx_days calculates the approximate days of travel. This is calculated from the number of days each train runs for and does not depend on waiting time.