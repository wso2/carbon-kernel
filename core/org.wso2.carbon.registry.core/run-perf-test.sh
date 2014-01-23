mvn test  -Dtest=BasicPerformanceTest -o > test.log
#egrep "jdbc.sqlonly" test.log  > test2.log
egrep "CSV," test.log  > stats.csv

