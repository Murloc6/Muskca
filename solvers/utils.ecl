forall(Cond, Action) :- \+ ( Cond, \+ Action).

numlist(X, X, [X]).
numlist(X, Y, L) :-
  X < Y,
  X2 is X +1,
  numlist(X2, Y, L2),
  append([X], L2, L).

sublist( [], _ ).
sublist(L, L2) :-
  length(L, SL),
  length(L2, SL2),
  SL =< SL2,
  forall(member(X, L), member(X, L2)).

get_couple(X, Y, L) :- member(X,L),member(Y,L), X\== Y.

all_different([]).
all_different([_]).
all_different([H|T]) :- not(member(H, T)), all_different(T).

test_not_sublist(_, []).
test_not_sublist(X, [H|T]) :-
  not(sublist(X, H)),
  test_not_sublist(X, T).

max_length([R], R).
max_length([X|Xs], R):- max_length(Xs, T), length(X, LX), length(T, LT), (LX > LT -> R = X ; R = T).


unify_list([], []).
unify_list([X|T],[X|Result]):-
  test_not_sublist(X, T),
  unify_list(T,Result).
unify_list([_|Tail],Result):-
   unify_list(Tail,Result).
