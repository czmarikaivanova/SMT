#!/bin/bash
exec 2>/dev/null
java -Djava.library.path=/opt/ibm/ILOG/CPLEX_Studio1251/cplex/bin/x86-64_sles10_4.1 -cp /opt/ibm/ILOG/CPLEX_Studio1251/cplex/lib/cplex.jar:. smt.Main $1 $2 $3 | tee output.txt |
awk -F '[[:space:]][[:space:]]+'  'BEGIN {
bb = systime(); elapsed = 0;currtime = 0; ref = 0; fc = 0; oldT=0; oldV=0; iter = 0; maxTime = 0;} /.*/ { 
	if (index($0, "New model started:") != 0) {
		print($0,"\n");
		bb=systime(); 
		elapsed = 0;
		if ($3 == 0) {
			iter = iter + 1;
			ref = 0;
		}
		outfile = "stat_" iter "_" $3 ".dat";
		oldT=0;
		oldV=0;
	}
	currtime = systime() - bb;
	if (currtime > elapsed || match($0,/Objective:/)) {
		elapsed = currtime;
		if (match($0,/^Objective:/)) {
			if (ref == 0) {
				ref = substr($0,index($0,":")+2,length($0));
			}
			else {
				if (elapsed - oldT > 1) {
					for (i=oldT+1; i < elapsed; i++) {
						printf("%d\t%.2f\n",i,oldV);
						printf("%d\t%.2f\n",i,oldV) > outfile;
					}
				}
				val=100*substr($0,index($0,":")+2,length($0))/ref
				printf("%d\t%.2f\n",elapsed,val);
				printf("%d\t%.2f\n",elapsed,val) > outfile;
				oldT=elapsed;
				oldV=val;
			}
		} else if (match($0,/^Iteration:/)) {
			if (elapsed - oldT > 1) {
				for (i=oldT+1; i < elapsed; i++) {
					printf("%d\t%.2f\n",i,oldV);
					printf("%d\t%.2f\n",i,oldV) > outfile;
				}
			}
			val=100*$(NF)/ref
			printf("%d\t%.2f\n",elapsed,val);
			printf("%d\t%.2f\n",elapsed,100*$(NF)/ref) > outfile;
			oldT=elapsed;
			oldV=val;
		} else if (match($0,/%$/)) {
			if (index($(NF-2),": ") == 0) {
				if (elapsed - oldT > 1) {
					for (i=oldT+1; i < elapsed; i++) {
						printf("%d\t%.2f\n",i,oldV);
						printf("%d\t%.2f\n",i,oldV) > outfile;
					}
				}
				val=100*$(NF-2)/ref
				printf("%d\t%.2f\n",elapsed,val);
				printf("%d\t%.2f\n",elapsed,val) > outfile;
				oldT=elapsed;
				oldV=val;
			}
		}
	}
#	if (elapsed > maxTime) {
#		maxTime = elapsed;
#	}
	# if there is some time left until the max time, fill the values with the last value
#	for (i=elapsed; i<maxTime; i++) {
#		printf("%d\t%.2f\n",i,val);
#	}
#} END {
#		print("maxTime: " maxTime);A
}'

echo "Determine max lines"
maxLines=0
for f in *.dat 
do
	linesCnt=$(wc -l < $f);
	if [ "$linesCnt" -gt "$maxLines" ]
	then
		maxLines=$linesCnt		
	fi
done
echo "fill the files"
for f in *.dat
do
	linesCnt=$(wc-l < $f)
	if [ "$linesCnt" -lt "$maxLines" ]
	then
		for i in {$linesCnt+1..$maxLines}
		do
			echo "$i\t x" >> $f
			echo "$i\t x" 
		done
	fi
done
fc=$(ls -1 ./plots/ | wc -l)
gnuplot -e "filename='plots/lower-bound-$1-$2-$fc.pdf'" plotscript

