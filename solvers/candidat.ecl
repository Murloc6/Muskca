%:- [data].
%:- include('utils.ecl').
%:- include('data_cand.ecl').
%:- include('data_cand_triticum.ecl').
:- include('solvers/utils.ecl').

rel(X, Y) :- mapping(X, Y).
rel(X, Y) :- mapping(Y, X).

diff_sources(X, Y) :- oe(SO1, LS), member(X, LS), oe(SO2, LS2), member(Y, LS2), SO1 \== SO2.
all_diff_sources(L) :- forall(get_couple(X, Y, L), diff_sources(X, Y)).

path_exists(X, Y, _) :-
  rel(X, Y).

path_exists(X, Y, L) :-
  path_exists(X, Y, L, []).

path_exists(X, Y, _, _) :-
  rel(X, Y).

path_exists(X, Y, L, V) :-
  member(Z, L),
  Z\==X,
  Z\==Y,
  not(member(Z, V)),
  append([Z], V, Vs),
  path_exists(X, Z, L, Vs),
  path_exists(Z, Y, L, Vs).


connexe(L) :-
  forall(get_couple(X, Y, L), path_exists(X, Y, L)).

max_cand_size(X) :-
  length(X, LX),
  LX > 1,
  sources(S),
  length(S, LS),
  LX =< LS.

append_sources([], []).
append_sources([Hs|Ts], L) :-
  oe(Hs, Ls),
  append_sources(Ts, R),
  append(R, Ls, L).

all_elem_in_list([], _).
all_elem_in_list([H|T], L) :-
  member(H, L),
  all_elem_in_list(T, L).



candidat([X, Y]) :-
  rel(X, Y),
  sources(Ls),
  append_sources(Ls, T),
  all_elem_in_list([X, Y], T).

candidat([Ch|Ct]) :-
  candidat(Ct),
  sources(Ls),
  append_sources(Ls, T),
  all_elem_in_list([Ch|Ct], T),
  max_cand_size([Ch|Ct]),
  all_different([Ch|Ct]),
  all_diff_sources([Ch|Ct]),
  connexe([Ch|Ct])
  .

%%%%%%%%%%%%%% TEST cand = list of mappings %%%%%%%%%%%
append_iff_not_exists(X, [], [X]).
append_iff_not_exists(X, [X|T], [X|T]).
append_iff_not_exists(X, [H|T], Lfin) :-
  X \== H,
  append_iff_not_exists(X, T, Ltemp),
  append([H], Ltemp, Lfin).

cand_list([], []).
cand_list([[X, Y]|T], Lc) :-
  cand_list(T, Lfin),
  append_iff_not_exists(X, Lfin, Ltemp),
  append_iff_not_exists(Y, Ltemp, Lc).
candidat_test([[X, Y]]) :-
  mapping(X, Y).
candidat_test([[X, Y]|Tc]) :-
  candidat_test(Tc),
  mapping(X, Y),
  max_cand_size([[X, Y]|Tc]),
  all_different([[X, Y]|Tc]),
  cand_list([[X, Y]|Tc], Lc),
  all_diff_sources(Lc),
  connexe(Lc).
%%%%%%%%%%%%%%%%%%% END TEST %%%%%%%%


candidat(X, Nb_cur) :-
  Nb_cur > 1,
  length(X, Nb_cur),
  candidat(X).

%candidat(X, Nb_cur,  []) :- candidat(X, Nb_cur).
%candidat(X, Nb_cur, L) :-
%  candidat(X, Nb_cur),
%  forall(member(C, L), not(sublist(X, C))).

all_candidates(Nbmax, L, Lfin) :-
  findall(X, candidat(X,Nbmax), Ltemp),
  unify_list(Ltemp, Lfin).

find_all_candidates(Nbmax, L, Lfin) :-
  findall(X, all_candidates(Nbmax, L, X), Ltemp),
  max_length(Ltemp, Lfin).


loop_nbMax([], L,  L).
loop_nbMax([Nb_cur|T_nb], L_cur,  L) :-
  find_all_candidates(Nb_cur, L_cur, L_fin),
  append(L_fin, L_cur, Ltemp),
  loop_nbMax(T_nb, Ltemp, L).

list_candidates(L) :-
  sources(S),
  length(S, S_max),
  numlist(2, S_max, Nb_cur_temp),
  reverse(Nb_cur_temp, L_nb),
  loop_nbMax(L_nb , [], L).

candidat_max(X) :-
   list_candidates(L),
   member(X, L).


%%%% Cand utils %%%%%

source_elem(S, U) :-
  oe(S, L),
  member(U, L).
