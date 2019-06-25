set datafile separator ","
set xrange [0:110]
set xlabel 'Num. of processes'
set ylabel 'Time(msec.)'
plot '2pc_summary.csv' every ::1 title "two-phase commit" with lines, \
    '2pc_summary.csv' every ::1 using 1:2:3:4 with errorbars pt 6 notitle,  \
    '3pc_summary.csv' every ::1 title "three-phase commit" with lines dt "-", \
    '3pc_summary.csv' every ::1 using 1:2:3:4 with errorbars pt 2 notitle

set output 'comparison.eps'
set terminal postscript eps color
replot
