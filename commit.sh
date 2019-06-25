#!/bin/sh

#numの値を変更すると試行回数が変化する

javac *.java

if ls 2pc_* > /dev/null 2>&1
  then
      rm ./2pc_*
fi
if ls 3pc_* > /dev/null 2>&1
  then
      rm ./3pc_*
fi

num=10
for ((i=10; i<110; i+=10)) ; do for ((j=0; j<$num; j++)) ; do java Simulator -c TwoPhaseCommit -t -p $i >> 2pc_"$i"P.txt; done; done
for ((i=10; i<110; i+=10)) ; do cat 2pc_"$i"P.txt | grep "msec" >> 2pc_"$i"P_time.txt ; done
echo "プロセス数,平均値,最大値,最小値" > 2pc_summary.csv
for ((i=10; i<110; i+=10)) ; do java CalcData 2pc_"$i"P_time.txt $num >> 2pc_summary.csv; done

for ((i=10; i<110; i+=10)) ; do for ((j=0; j<$num; j++)) ; do java Simulator -c ThreePhaseCommit -t -p $i >> 3pc_"$i"P.txt; done; done
for ((i=10; i<110; i+=10)) ; do cat 3pc_"$i"P.txt | grep "msec" >> 3pc_"$i"P_time.txt ; done
echo "プロセス数,平均値,最大値,最小値" > 3pc_summary.csv
for ((i=10; i<110; i+=10)) ; do java CalcData 3pc_"$i"P_time.txt $num >> 3pc_summary.csv; done

echo "**********   2PC   **********"
cat 2pc_summary.csv

echo ""

echo "**********   3PC   **********"
cat 3pc_summary.csv

rm ./2pc_*P_time.txt
rm ./3pc_*P_time.txt

gnuplot "comparison.plt"

open comparison.eps