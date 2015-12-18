# Introduction
The tool provides support for generating, quantifying and ranking misuse scenarios directly from a business model of a given service.  
We define misuse as those situations in which actors may not perform transactions that they committed to, perform secret transactions, and/or collude with other actors. 

- The tool can generate misuse scenarios based on configurable heuristics. 
- Furthermore, it can group and rank such scenarios on various criteria, such as loss to a service provider or profit to a fraudster. 
- Finally, it can help visualize the financial results across a range of projected usage levels.


#  BASIC USAGE  
0. create e3value model (using e3value toolkit at: http://e3value.few.vu.nl/) and export it as RDF.
1. Press LOAD MODEL to select an RDF file to analyze
2. press GENERATE SUB-IDEAL MODELS and select.
3. You will be prompted to indicate: the Target of Assessment, the usage indicatort to use for the analysis its estimated range.
4. Results will appear as scrollable list. Clicking on a result and then on the > bar will open the profitability graph for that result.
5. The ranking and grouping settings can be changed using the left-had side panel and clicking "Refresh"



# TECHNICAL DETAILS 
##  Generation  
- Hidden transactions are generated by identifying pairs of secondary (non-ToA) actors, computing their expected (i.e. ideal) profitability and then creating sub-ideal models with new transfers in each direction between the two actors. 
The transfers take values ranging between zero and  expected profitability of the outgoing actor. 
- Non-occurring transactions are created by invalidating individual monetary transfers (that is, transfers marked as type $MONEY$). This is to limit sate space explosion. Monetary transfers which are not to be invalidated, either because they are initiated by the provider itself,  because  safeguards are in place or simply to reduce the search space of sub-ideal models, can be marked as as type $MONEY-SEC
- Collusion takes place when two actors are acting as one: they pool their budgets and collectively bear all expenses and profit. Only secondary actors (not the ToA) can collude. The number of actors allowed to collude is configurable.


## 	Ranking 
Depending on the complexity of the initial ideal model, hundreds of even thousands of models might be generated. Most of these might not describe a profitable or feasible fraud or misuse scenario. 

To aid with selection and prioritization, the tool provides several ways of ranking and grouping the set of generated models.The prioritization is always carried out from the perspective of a single actor (the Target of Assessment), as described below.

The software tool allows ranking based on Loss (for the ToA), Gain (defined as the difference between the financial result of a secondary actor in the ideal case versus the sub-ideal case) and Loss+Gain (where both these factors are combined into a hybrid measure).

Furthermore, to allow for "what-if" analyses and easier navigation through the long list of sub-ideal models, results can be grouped (and collapsed) based who is colluding with who. 
This allows listing misuse scenario per attacker.

## Visualization
The ranked list of generated sub-ideal models can be viewed as textual descriptions. If grouping was used, the list is nested. This facilitates the exploration of the state space.

The financial results of the ideal models or any of the sub-ideal modes can also be visualized as a 2D plot with the desiered usage indicator on the X-axis and financial result on the Y-axis

Both kinds of representations can be used by marketers and product managers without having to learn \emph{e3value} or \emph{e3fraud}.
The results can be used to quantitatively asses the impact of the risk (in terms of loss for the ToA), the likelihood of the risk (in terms of gain for secondary actors) as well as the evolution of these factors across a given usage range. 
The user may select which usage indicator to be represented on the X-axis, as well as its range.