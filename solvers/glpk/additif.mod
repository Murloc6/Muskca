param n>0 integer; /* the number candidate*/
set I := 1..n;
param trust{i in I};
param conflict{ i in I, j in I};#1 si confi
param trustArc{i in I, j in I}; # sum arc cands trust shared
param defined{i in I};

/* Decision variables */
var x{i in I},  binary; #zero si selectionne 1 sinon

/* Objective function */
/* maximize z: sum{i in I}trust[i]*x[i];*/
maximize z: sum{i in I}(x[i]+(trust[i]+sum{j in I: j>i}trustArc[i, j]*x[j]));

/* Constraints */
s.t.Cconfict{i in I, j in I}:conflict[i,j]*(x[i]+x[j])<=1;
/*Cdefined{i in I}:if defined[i] == 1 then x[i] == 1 else if defined[i] == 0 then x[i] == 0;*/
s.t.Cdefined{i in I: defined[i]!=-1}:x[i] == defined[i];

/*
solve;
printf "SOlution : ";
printf {i in I: x[i] == 1} " %i", i;
printf "\n";
--cover --clique --gomory --mir
*/
end;
