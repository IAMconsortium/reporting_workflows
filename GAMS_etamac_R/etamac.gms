$Title Eta-Macro Energy Model for the USA (ETAMAC,SEQ=80)

$Ontext

   This is an Energy Macro Economic Interaction model for the United
   States developed by Prof A Manne, Stanford University.


Manne, A S, ETA-MACRO: A Model of Energy-Economy Interactions. In Hitch,
C J, Ed, Modeling Energy-Economy Interactions, Resources for the Future.
?, Washington, DC, 1977.

*------------------------------------------------------------------------
  These are notes on changing the time horizon and number of years per period.

  You must first enter the set t which is the time periods that will be
  used.  The number of years between entries in t must be the value nyper.
  you must choose the number of years per period, nyper, and this must
  correspond to the set t. You must change the number of years per period,
  nyper, in two places. nyper must be greater than or equal to 2.

  You must also enter the set inityrs which contains the years from the base
  year to the year before the first year.

   Units Used:

   Electric Energy                10**12 kwh
   Non-Electric Energy            10**15 btu
   Price of Electric Energy     $/(10**3 kwh)
   Price of Non-Electric Energy $/(10**6 btu)
   GNP                              10**12 $
*------------------------------------------------------------------------

$Offtext


 Scalars  nyper  number of years per period / 5 /;

 Sets     inityrs   years before first year / 1985*1989 /
          bsyr      base year
          t         actual time periods  / 1990, 1995, 2000, 2005, 2010,
                                           2015, 2020, 2025, 2030 /
          tfirst(t) first period
          tlast(t)  last period
          nypset    set from 1 to nyper / 1*5 /;

 bsyr(inityrs) = yes$(ord(inityrs) eq 1);
 tfirst(t) = yes$(ord(t) eq 1);
 tlast(t) = yes$(ord(t) eq card(t));


*------------------------------------------------------------------------
 Scalars  spda   speed of adjustment              /  0.96 /
          kpvs   capital share parameter          /  0.28 /
          elvs   electric share parameter         /  0.35 /
          esub   elasticity between k-l and e-n   /  0.45 /
          k0     initial capital                  / 10.90 /
          e0     initial electric energy          /  2.50 /
          n0     initial non-electric energy      / 50    /
          i0     initial investment               /  0.7  /
          c0     initial consumption              /  3.2  /
          pe0    initial price of electric energy       / 50   /
          pn0    initial price of non-electric energy   /  4.5 /
          pnref  reference price of non-electric energy /  3.0 /
          y0     initial output
          htrt0  initial heat rate
          thsnd  one thousand                         / 1000.0 /
          rho    esub minus one divided by esub
          aconst constant for capital-labor index
          bconst constant for electric-non-electric energy index
          ninit  number of years before the first year
          tol    tolerance factor for lower bounds      / 0.3 /;
*------------------------------------------------------------------------


 Parameters      dfactcurr(t)    current annual utility discount factor
                 dfact(t)        utility discount factor
                 grow(t)         potential annual gnp growth rate
                 pegrow(t)       current growth of electricity price
                 pelec(t)        growth of electricity price
                 pngrow(t)       current growth of non-electricity price
                 pnelec(t)       growth of non-electricity price
                 l(t)            current labor force (efficiency units)
                 ln(t)           new labor force
                 ipm(t)          investment period multiplier
                 htrt(t)         heat rate
                 knew(t)         new capital stock;


*------------------------------------------------------------------------
* the following input factors refer, respectively, to utility discounting
* (dfactcurr), growth of electric and nonelectric energy costs (pelec and
* pnelec), and of potential gnp (grow).

 dfactcurr(t)  = 0.96;
 pegrow(t)     = 0.01;
 pngrow(t)     = 0.02;
 grow(t)       = 0.03;
 htrt0         = 10.809;
 htrt(t)       = 10.809;
*------------------------------------------------------------------------

 ninit = card(inityrs);
 rho   = (esub - 1)/esub;
 y0    = i0 + c0 + (e0*pe0 + n0*pn0)/thsnd;

 bconst = (pnref/thsnd)*y0**(rho - 1) /
          ((1 - elvs)*(e0**(rho*elvs))*(n0**(rho*(1 - elvs) - 1)));

 aconst = (y0**rho - bconst*(e0**(rho*elvs))*(n0**(rho*(1 - elvs)))) /
          (k0**(rho*kpvs));

*the following calculations allow for the growth of investment within each
*period, and also for its geometric decay.

 knew(tfirst) = i0*(sum(inityrs, spda**(ord(inityrs) - 1)*
                        (1 + grow(tfirst))**(ninit - ord(inityrs))));

 ipm(t) = sum(nypset, spda**(ord(nypset) - 1)*
                      (1 + grow(t))**(nyper - ord(nypset)));


 dfact(tfirst)  = dfactcurr(tfirst)**ninit;
 l(tfirst)      = (1 + grow(tfirst))**ninit;
 ln(tfirst)     = l(tfirst) - (spda**ninit);
 pelec(tfirst)  = pe0*((1 + pegrow(tfirst))**ninit);
 pnelec(tfirst) = pn0*((1 + pngrow(tfirst))**ninit);

 Loop(t, dfact(t+1)  = dfact(t)*dfactcurr(t+1)**nyper ;
         l(t+1)      = l(t)*(1 + grow(t+1))**nyper ;
         ln(t+1)     = l(t+1) - l(t)*(spda**nyper) ;
         pelec(t+1)  = pelec(t)*(1 + pegrow(t))**nyper ;
         pnelec(t+1) = pnelec(t)*(1 + pngrow(t))**nyper );

 dfact(tlast)  = dfact(tlast)/(1-dfactcurr(tlast));

 Display ipm, kpvs, elvs, l, ln, rho, aconst, bconst, pelec, pnelec, knew;


 Variables k(t)    capital stock
           kn(t)   new capital stock
           y(t)    production
           yn(t)   new production
           e(t)    electric energy
           en(t)   new electric energy
           n(t)    non-electric energy
           nn(t)   new non-electric energy
           c(t)    consumption
           i(t)    investment
           ec(t)   energy cost in trillions
           utility;


 k.l(t) = k0*l(t);
 y.l(t) = y0*l(t);
 e.l(t) = e0*l(t);
 n.l(t) = n0*l(t);
 c.l(t) = c0*l(t);
 i.l(t) = i0*l(t);

 Display k.l, y.l, e.l, n.l, c.l, i.l;


 Equations newcap(t)           new capital
           newprod(t)          new production
           fnewelec(t)         new electric energy in first period
           newelec(t)          new electric energy
           fnewnon(t)          new non-electric energy in first period
           newnon(t)           new non-electric energy
           totalcap(t)         total capital stock
           ftotalprod(t)       total production in first period
           totalprod(t)        total production
           costnrg(t)          cost of energy
           cc(t)               capacity constraint
           tc(t)               terminal condition
           util                discounted log of consumption;


 newcap(t+1)..       kn(t+1) =e= i(t)*ipm(t);

 newprod(t+1)..      yn(t+1) =e= (aconst*(kn(t+1)**(rho*kpvs)) *
                                 (ln(t+1)**(rho*(1 - kpvs))) +
                                 bconst*(en(t+1)**(rho*elvs)) *
                                 (nn(t+1)**(rho*(1 - elvs)))) ** (1/rho);


 fnewelec(tfirst)..  en(tfirst) =e= e(tfirst) - e0*(spda**nyper);

 newelec(t+1)..      en(t+1) =e= e(t+1) - e(t)*(spda**nyper);


 fnewnon(tfirst)..   nn(tfirst) =e= n(tfirst) - n0*(spda**nyper);

 newnon(t+1)..       nn(t+1) =e= n(t+1) - n(t)*(spda**nyper);


 totalcap(t+1)..     k(t+1) =e= k(t)*(spda**nyper) + kn(t+1);


 ftotalprod(tfirst)..y(tfirst) =e= y0*(spda**ninit) +
                                   (aconst*(knew(tfirst)**(rho*kpvs)) *
                                   (ln(tfirst)**(rho*(1-kpvs))) +
                                   bconst*(en(tfirst)**(rho*elvs)) *
                                   (nn(tfirst)**(rho*(1 - elvs)))) ** (1/rho);

 totalprod(t+1)..    y(t+1) =e= y(t)*(spda**nyper) + yn(t+1);


 costnrg(t)..        thsnd*ec(t) =e= pelec(t)*e(t) + pnelec(t)*n(t);

 cc(t)..             y(t) =e= c(t) + i(t) + ec(t);

 tc(tlast)..         k(tlast)*(grow(tlast) + (1 - spda)) =l= i(tlast);

 util..              utility =e= sum(t, dfact(t)*log(c(t)));

 Model etamac /all/;

 k.lo(t)  = k0;
 kn.lo(t) = tol*i0*ipm(t);
 y.lo(t)  = y0;
 yn.lo(t) = tol*y0*ln(t);
 e.lo(t)  = e0;
 en.lo(t) = tol*e0*ln(t);
 n.lo(t)  = n0;
 nn.lo(t) = tol*n0*ln(t);
 c.lo(t)  = c0;
 i.lo(t)  = i0;

 k.fx(tfirst) = k0*(spda**ninit) + knew(tfirst);

 Solve etamac maximizing utility using nlp;

$Stitle report definitions

 Parameter  valuerep    report for c-i-gdp-e-en-tpe
            growthrep   report of growth rates;

    Execute_Unload 'fulldata';

 valuerep("con",   bsyr) = c0;
 valuerep("inv",   bsyr) = i0;
 valuerep("gdp",   bsyr) = c0 + i0;
 valuerep("elec",  bsyr) = e0;
 valuerep("nelec", bsyr) = n0;
 valuerep("tpe",   bsyr) = htrt0*e0 + n0;

 valuerep("con",   t) = c.l(t);
 valuerep("inv",   t) = i.l(t);
 valuerep("gdp",   t) = c.l(t) + i.l(t);
 valuerep("elec",  t) = e.l(t);
 valuerep("nelec", t) = n.l(t);
 valuerep("tpe",   t) = htrt(t)*e.l(t) + n.l(t);


 growthrep("con", "'85-00")  = 100*((c.l("2000")/c0)**(1/15) - 1);
 growthrep("inv", "'85-00")  = 100*((i.l("2000")/i0)**(1/15) - 1);
 growthrep("gdp", "'85-00")  = 100*(((c.l("2000") + i.l("2000"))/
                               (c0 + i0))**(1/15) - 1);
 growthrep("elec", "'85-00") = 100*((e.l("2000")/e0)**(1/15) - 1);
 growthrep("nelec","'85-00") = 100*((n.l("2000")/n0)**(1/15) - 1);
 growthrep("tpe",  "'85-00") = 100*(((htrt("2000")*e.l("2000") + n.l("2000"))/
                                (htrt0*e0 + n0))**(1/15) - 1);

 growthrep("con",  "'00-20") = 100*((c.l("2020")/c.l("2000"))**(1/20) - 1);
 growthrep("inv",  "'00-20") = 100*((i.l("2020")/i.l("2000"))**(1/20) - 1);
 growthrep("gdp",  "'00-20") = 100*(((c.l("2020") + i.l("2020"))/
                               (c.l("2000") + i.l("2000")))**(1/20) - 1);
 growthrep("elec", "'00-20") = 100*((e.l("2020")/e.l("2000"))**(1/20) - 1);
 growthrep("nelec","'00-20") = 100*((n.l("2020")/n.l("2000"))**(1/20) - 1);
 growthrep("tpe",  "'00-20") = 100*(((htrt("2020")*e.l("2020") + n.l("2020"))/
                               (htrt("2000")*e.l("2000") + n.l("2000")))
                               **(1/20) - 1);


 Display valuerep, growthrep;
