
%:- include('utils.ecl').
%:- include('data_ind_candidates.ecl').
%:- include('data_ext.ecl').

:- include('solvers/utils.ecl').
%:- include('solvers/data_ext.ecl').

incompatible(C1, C2) :-
  candidat(C1),
  candidat(C2),
  C1 \= C2,
  member(X, C1),
  member(X, C2).

all_compatible([_]).
all_compatible([H|T]) :-
  all_compatible(T),
  forall(member(X, T), not(incompatible(X, H))).

all_candidat([]).
all_candidat([H|T]) :-
  candidat(H),
  all_candidat(T).

different_from(_, []).
different_from(X, [H|T]) :-
  different_from(X, T),
  X \== H.

compatible_from(_, []).
compatible_from(X, [H|T]) :-
  compatible_from(X, T),
  not(incompatible(X, H)).

extension([X]) :- candidat(X).
extension([H|T]) :-
  extension(T),
  candidat(H),
  different_from(H, T),
  compatible_from(H, T).


test_extension([], 0).
test_extension([H|Ext_temp], Nb_cur) :-
  Nb_temp is Nb_cur - 1,
  test_extension(Ext_temp, Nb_temp),
  extension([H|Ext_temp]), !.


extension(X, Nb_cur) :-
  length(X, Nb_cur),
  extension(X).

extension(X, Nb_cur,  []) :- extension(X, Nb_cur).
extension(X, Nb_cur, L) :-
  extension(X, Nb_cur),
  forall(member(C, L), not(sublist(X, C))).

all_extensions(Nbmax, L, Lfin) :-
  findall(X, extension(X,Nbmax, L), Ltemp),
  unify_list(Ltemp, Lfin).

find_all_extensions(Nb_cur, L_cur, L_fin) :-
  findall(X, all_extensions(Nb_cur, L_cur, X), L_temp),
  max_length(L_temp, L_fin).

loop_nbMax([], L,  L).
loop_nbMax([Nb_cur|T_nb], L_cur,  L) :-
  find_all_extensions(Nb_cur, L_cur, L_fin),
  append(L_fin, L_cur, L_temp),
  loop_nbMax(T_nb, L_temp, L).

list_extensions(X, Nb_cand) :-
  numlist(1, Nb_cand, L_temp),
  reverse(L_temp, L_nb),
  loop_nbMax(L_nb, [], X).

extension_max(X, Nb_cand) :-
  list_extensions(L, Nb_cand),
  member(X, L).
