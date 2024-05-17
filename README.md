# fx-java-trading-strategy
A graphical user interface to run and test trading strategies by combining different technical indicators. Written in Java/swing.

Important
=========
Please read the **LICENSE** section below.
Unless you accept its terms and conditions, you are strictly prohibited to make use of this software
or source codes (directly or indirectly).

To launch the application
=========================
Running from IDE:
Start "net.lexzeus.fx.simul.MainFrame" as a normal java application

If you have the jar file, run:
java -classpath "trading-strategy-1.0.0-SNAPSHOT.jar" net.lexzeus.fx.simul.MainFrame

To avoid running out of memory error when running the application against large market
data, you may need to adjust the java memory heap by specifying the Xms and Xmx directive,
for example:

java -Xms512m -Xmx1G -classpath "trading-strategy-1.0.0-SNAPSHOT.jar" net.lexzeus.fx.simul.MainFrame

![Screenshot 1](https://github.com/tummybunny/fx-java-trading-strategy/blob/main/images/Screenshot1.jpg)


![Screenshot 2](https://github.com/tummybunny/fx-java-trading-strategy/blob/main/images/Screenshot2.jpg)


![Screenshot 3](https://github.com/tummybunny/fx-java-trading-strategy/blob/main/images/Screenshot3.jpg)

Where can I get the market data?
================================
Sample market data is in the \marketDataSamples directory.
You can obtain from other sources but it must adhere to this format:
DD/MM/YYYY,HH:MM:SS,volume,open_price,closing_price,low,high

For example:

28/10/2008,00:00:01,1,1.2466,1.2467,1.2466,1.2467

28/10/2008,00:00:11,2,1.2466,1.2467,1.2466,1.2467

28/10/2008,00:00:21,1,1.2467,1.2468,1.2467,1.2468

28/10/2008,00:00:31,2,1.2468,1.2468,1.2467,1.2468



Using Trading Strategy Runner Application
=========================================
1. Launch the application.

2. Menu File -> Open Market Data (Ctrl+O), choose the market data csv file you want to open.

3. If the chart looks too crowded, there are few things that you can do:
   a. Turn off the candle stick view by un-ticking "Candle" check box.
   b. Adjust chart interval: change the YELLOW combo box from Tick to Min (Minute), Hour, or Day,
   and the nominal value.
   c. Change the From and To window.
   d. Press REFRESH button. Your changes will not be reflected until you press REFRESH.

4. Start adding technical indicator:
   a. The first green combo box allows you to choose between opening, closing, high, or low price of
   market data.
   b. The second green combo box allows you to choose technical indicator available in the application.
   Some of them are EMA, SMA, RSI, ADx, Bollinger Band, +DI, -DI, WMA, and envelopes.
   The "Line" is not a technical indicator, but a way to connect market data points with lines.
   c. The square next to the second green combo box is the color picker. Press this square to change
   the technical indicator color.
   d. Next, enter the parameters for the selected technical indicator.
   Each indicator may require 0, 1, n parameters, delimited by space.
   Supplying more parameters than required will not influence the indicator.
   o Line: does not accept any parameter
   o EMA: accepts 1 parameter
   o SMA: accepts 1 parameter
   o RSI: accepts 1 parameter
   o ADX: accepts 1 parameter
   o +DI: accepts 1 parameter
   o -DI: accepts 1 parameter
   o Bollinger Band: accepts 2 parameters. The second parameter is optional.
   o Envelopes: accepts 2 parameters. The second parameter is mandatory.

   If you want to have Bollinger Band for 14 periods with 2 standard deviation, enter "14 200"
   (without double quotes). Standard deviation is specified in a hundredth, i.e. 200 means 2, and 150
   means 1.5. By default 2 standard deviation is used when the second parameter is omitted.

   Envelopes uses 2 parameters: the moving average period, and the envelope value. The
   envelope value is in ten thousandth, i.e. 20 means 0.0020 and 100 means 0.01.

   e. Next, press the Add button. The add button will add the selected technical indicator into the
   chart. Adding a technical indicator when existing indicator with identical parameters already
   displayed on the chart will replace the existing one. You want to do this when you want to change
   the color.
   f. To remove specific technical indicator from the chart, you have to choose from the same technical
   indicator name and parameters that you want to remove, and press Remove button.
   The name of all indicators shown in the chart along with their parameter are displayed at the
   bottom.
   g. To remove all technical indicators from the chart, press Clear.

5. You may want to record a Profit and Loss. To do this:
   a. Click "Record P&L" button once, it will turn into red.
   b. From now on, left click on the chart means BUY at that specified date and price for quantity 1,
   and right click means SELL.
   To simulate trading activities, you perform left/right click on the chart.
   c. After you make a series of BUY and SELL clicks, press the "Record P&L" button once again.
   This time you will be prompted with a Save P&L dialog box, and click "Save" to save the
   recording.

6. In step 5, we manually create and close position using left and right click. Alternatively,
   we can run a Trading Strategy.
   a. Remove all technical indicators from the chart, uncheck both the CANDLE and LINE boxes, and
   press REFRESH. Doing so, you should see a blank chart.
   This step is optional, however having too many things on the chart might make it difficult to
   trace the BUY and SELL events.
   b. Make sure the "Record P&L" button is not activated.
   c. Double click anywhere inside the chart and you will be prompted with a Strategy window.
   By default, the active strategy is NONE, meaning that you have not selected one yet.
   d. In this example, our goal is to have the EMA 10 vs EMA 20 cross-over strategy to produce BUY
   and SELL signals.

    1. Select "EmaCrossBuySellSignal" strategy from the combo box and input "10 20" (without
       double quotes) in the Indicator values.
       For simplicity sake, enter 0 (zero) in take profit, cut loss, and dynamic cut loss.
       Note: dynamic cut loss is similar to trailing cut loss - it will only be activated once we
       make a profit.

    2. Click Apply button. Clicking this button for the first time shows EMA 10, EMA 20, Closing Price
       line, and most importantly, the PROFIT and LOSS lines.
       Clicking the same button for the second time only show you Closing Price line, and the PROFIT
       and LOSS lines.

    3. The GREEN lines indicate PROFITS, and RED lines indicate LOSSES.
       Note: Both profits and losses can be generated from long or short positions. For example, if we open
       a short position and then close the position after price falls, we make a profit (GREEN line).

    4. To save the P&L result, press "Save P&L" button. A save P&L dialog will appear, click Save
       button to save the result.

    5. To quickly run the strategy and come out with net profit / loss number, click the TEST button.

7. You may want to save the chart, the technical indicators, and chosen trading strategy by using menu
   File -> Save Snapshot (Ctrl+S).

8. To load a snapshot you can access menu File -> Load Snapshot (Ctrl+L)



Adjusting Starting And Ending Period of Market Data
===================================================
You can adjust the starting and ending period by changing the value of From and To fields.
Alternatively, you can hold the CTRL key, and use mouse to left click and drag specific range on the chart
horizontally. The starting and ending period will be updated automatically.
To reset the period to its original period, hold the CTRL key and perform double left click on the chart.


Advanced Topics
===============
1. Implementing Your Own Technical Indicators.
   You need to be familiar with Java programming language to implement your own technical indicators.

   a. Create a technical indicator class by extending from net.lexzeus.fx.simul.indicators.TechnicalIndicator

   You generally need to implement two methods:
    - getName() : to give your technical indicator a name.
    - applyIndicatorImpl() : the technical indicator computation logic.

   b. You can use net.lexzeus.fx.simul.PricePlotter to find specific chart coordinate
   given a value and a timestamp. You generally do this, for example, when trying to connect
   an indicator value A at T to a value B at T+n.
   The T and n will depend on the chart interval type (TICK, MIN, HOUR, DAY) and the value.

   c. If you are building momentum type technical indicator such as RSI or something that
   requires some strength signals (between 0 to 100). You need to implements:
   net.lexzeus.fx.simul.IMomentumType

   d. Finally, to include your new custom indicator in the application, you need to modify:
   src/main/resources/TradingStrategyConfiguration.properties
   Add:
   indicator.nn.name=CUSTOM1
   indicator.nn.class=net.lexzeus.fx.simul.indicators.MyCustomIndicator


2. Implementing Your Own Buy/Sell Strategy.
   You need to be familiar with Java Programming to implement your own buy/sell strategies.

   a. Create a buy/sell strategy class by extending from net.lexzeus.fx.simul.strategies.BuySellStrategy

   You generally need to implement or override existing methods:
    - getIndicatorCount() : the number of technical indicator used in your strategy.
      For example, if you want to have two EMAs cross-over to generate BUY/SELL signal, the
      value is 2 because you want to track 2 EMAs.
    - computeTechnicalIndicator() : you need to compute the technical indicators here.
    - getEffectiveMarketDataStartingPosition() : tick index at which this Strategy should start
      giving directional signal. For example, if you use RSI 30, it is more sensible if buy/sell
      signals only be produced after 30th bar is received (hence you have the correct RSI 30
      value).
    - getFastIndicatorValue(int) and getSlowIndicatorValue(int): the default buy/sell signal
      strategy generation is to cross fast moving technical indicator value with the slow one.
      For example: crossing closing price with SMA 20, the fast indicator value is the closing
      price, and the slow one is value of SMA 20.

   To change the default buy/sell signal strategy, you will need to override
   formulateNewDirection(int, Tick, SignalDirection).

   Example:
   You want to have 2 EMAs cross over strategy to generate buy/sell signals, however you
   want to take neutral position when market is trading within some Bollinger Band ranges.

   Solution:
    1) Have the getIndicatorCount() to return 5 indicators.
       Why is it 5? Should it be 3?
       It is because Bollinger Band uses 3 indicators: an SMA, the upper band, and
       the lower band. Therefore, there are total of 5 indicators:
       EMA I (fast), EMA II (slow), SMA for Bollinger Band, upper Band, and lower Band

    2) Accept 4 strategy parameters as input:
       1'st parameter: period for EMA I (fast)
       2'nd parameter: period for EMA II (slow)
       3'rd parameter: period for Bollinger Band
       4'th parameter: the standard deviation

   With this parameters, we can enter "20 50 20 2" for EMA 20 vs EMA 50 with Bollinger Band period 20
   and 2 standard deviation.

    3) Have the computeTechnicalIndicator() to compute EMA I, EMA II, and Bollinger Band using
       all the 4 parameters.

   To obtain the parameter:
   strategyParameter.getStrategyValue(int)
   And supply the index, i.e. 3 for getting the 3'rd parameter.

   You can use the following methods to compute EMA and Bollinger Bands:
   BuySellStrategy.ema(int, int, Graphics, PricePlotter)
   BuySellStrategy.bollinger(int, int, int, int, double, Graphics, PricePlotter)

    4) Have getEffectiveMarketDataStartingPosition() to return maximum value of 2'nd and 3'rd
       parameter, so that the indicator values of EMA II and Bollinger Band are fully calculated
       when producing buy/sell signals.

    5) Have getFastIndicatorValue(int) to return get value of EMA I, and have
       getSlowIndicatorValue(int) to return get value of EMA II

    6) Override formulateNewDirection(int, Tick, SignalDirection) to compare the current closing
       price with the upper and lower Bollinger Bands.
       Have the method to return Neutral when the price is within the upper and lower bands, and
       return super.formulateNewDirection(int, Tick, SignalDirection) if otherwise.

   The source code of EMA crossover + Bollinger Band is already available at:
   src/main/net/lexzeus/fx/simul/strategies/BollingerEmaCrossBuySellStrategy.java

   By understanding how BollingerEmaCrossBuySellStrategy produces buy / sell signal, you can
   easily your own trading strategy.

   b. If you are building trading strategy using momentum-type indicators such as RSI and ADX, you need
   to implement net.lexzeus.fx.simul.IMomentumType

   c. To have your custom strategy be included as selectable buy/sell strategy in the application,
   you need to modify:
   src/main/resources/TradingStrategyConfiguration.properties
   Add:
   strategy.nn.name=AlwaysWinning
   strategy.nn.class=net.lexzeus.fx.simul.strategies.AlwaysWinningStrategy


LICENSE
=======

MIT License

Copyright (c) 2024 Alexander Koentjara

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
