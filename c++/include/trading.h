//////////////////////////////////////////////////////////////////////
// Zorro-specific variables & functions
// for use by lite-C scripts & Zorro program
// (c) oP group 2013, 2016
//////////////////////////////////////////////////////////////////////
#ifndef trading_h
#define trading_h

/////////////////////////////////////////////////////////////////////
// convenience definitions C/C++ -> lite-C
typedef double var;
typedef var* vars;
typedef char* string;
typedef long function;

#define as_int(x) *((int*)&(x))
#define PI	3.14159265359

/////////////////////////////////////////////////////////////////////
// constants (don't change!)
#define NAMESIZE		16
#define NAMESIZE2		32
#define NUM_SIGNALS	20
#define NUM_SKILLS	8
#define NUM_RESULTS	20
#define MAX_PARAMS	16		// max optimize() calls
#define MAX_STEPS		1024	// max optimize() steps

/////////////////////////////////////////////////////////////////////
// trading structs
typedef struct TICK
{
	float fOpen, fClose;	
	float fHigh, fLow;	
	DATE	time;	// time stamp, GMT
} TICK; // .bar file content

typedef struct T1
{
	DATE	time;	// time stamp, GMT
	float fVal;	
} T1; // single-stream tick, .t1 file content

typedef struct T2
{
	DATE	time;	
	float fPrice;
	float fVolume;	// negative = bid price
} T2; // quote

typedef struct THL
{
	DATE	time;	
	float fHigh, fLow;	
} THL; // two-stream tick, internal format

typedef struct T6
{
	DATE	time;	
	float fHigh, fLow;	
	float fOpen, fClose;	
	float fVal, fVol; // optional data, like spread and volume
} T6; // 6-stream tick, .t6 file content

typedef struct CONTRACT
{
	DATE	time;	
	float fAsk, fBid; 
	short	Type;
	short	Expiry;
	float fStrike;
	float fVal;		// f.i. OpenInterest; 
	float fVol; 
} CONTRACT; // options, futures, etc.

typedef struct BAR
{
	DATE	time_base,time_span;		// start and duration of the bar in GMT time zone
} BAR;

typedef struct DATA
{
	int	start,end;	// first and last plotted data in the array
	var*	Data;			// data array
} DATA;

typedef struct MATRIX
{
  int rows, cols, total;
  var* dat;
} MATRIX;

typedef MATRIX*  mat;
#define me(M,row,col)	*(M->dat + row*M->cols + col) 
#define ve(M,n)			*(M->dat+n) 

//dummy function to preset external structs for CS_GetVar() and sizeof()
#ifndef WIN32
void lcStructs()
{
	RECT* r = 0; r->left = 0;
	TICK* t = 0; t->time = 0;
	T1* t1 = 0; t1->time = 0;
	T2* t2 = 0; t2->time = 0;
	T6* t6 = 0; t6->time = 0;
	THL* thl = 0; thl->time = 0;
	BAR* b = 0; b->time_base = 0;
	DATA* d = 0; d->Data = 0;
	MATRIX* m = 0; m->dat = 0;
	CONTRACT* c = 0; c->time = 0;
}
#endif
//////////////////////////////////////////////////////////////
// objects
typedef struct ASSET
{
	var	vPrice;	// current price; last price for trade entry/exit
	var	vSpread; // difference between buy and sell prices
	var	vRollLong,vRollShort;	// interest for short and long position rollover per 10K
	var	vPIP;		// conversion factor Pip -> Price (0.0001 for most currencies, 1 for most CFDs)
	var	vPIPCost;	// cost of one Pip per lot, in account currency
	var	vMarginCost; // broker's margin cost per lot (usually between 5 and 100), determining the leverage
	var	vLotAmount;	// number of contracts per lot
	char	sName[NAMESIZE];	// name of the asset
	var	*pOpen,*pClose,*pHigh,*pLow,*pPrice;	// price history data array
	void	**pTicks;	// tick pointer array, used for tick simulation in trade functions
	void	*tick_start,*tick_end;	// tick list in THL or T6 format, dependent on EXTRADATA
	int	nFirstPriceBar;	// first simulation bar for this asset
	int	nLastPriceBar;		// last simulation bar for this asset
	int	nBar,nCounter,nOffset;
	var	vBarAvg;		// Average price of one bar, intermediate value
	int	nMaxLots;	// Max # of lots per trade
	int	flags;
	int	nSkipped,nFrame;	// distance to the last bar with a price quote
	int	nNetSum,nPhantomSum; // number of lots of all open trades
	int	nExtSum;			// number of lots of externally closed trades
	var	vFactor,vOffset;	// for normalizing price ticks
	var	vCommission;	// commission per 10K, in account currency
	var	Skill[NUM_SKILLS]; // asset-specific general purpose variables
	var	vSlipPerSec;	// price difference per second by slippage
	var	vBack[6];		// price backup for intrabar simulation
	var	vWinPayout,vLossPayout;	// payouts in percent for binary trading
	int	nZone,nHour;	// asset time zone
	var	vLeverage;	// buying power
	char	sCCY[4];		// counter currency (not used yet)
	char	sSymbol[NAMESIZE2];	// special broker name of that asset
	var	vInitialPrice;	// from the asset parameters
	var	vStrength;		// currency strength difference
	var	vVal,vVol;		// volume data or quote frequencies
	var	*pVal,*pVol;	// additional price data lists, f.i. spread and volume
} ASSET;

typedef struct TRADE
{
	float	fEntryPrice;	// buy price per unit
	float	fExitPrice;	// sell price per unit, without spread
	float fResult;		// current profit of the trade
	float	fEntryLimit;	// buy entry limit
	float	fProfitLimit;	// profit limit price
	float fTrailLock;	// profit target distance to initial price, positive (long) or negative (short)
	float	fStopLimit;	// stop loss limit price
	float	fStopDiff;	// signed stop loss distance to initial price, positive (short) or negative (long)
	float	fTrailLimit;	// trail limit price
	float	fTrailDiff;	// signed stop loss adaption distance, positive (short) or negative (long)
	float	fTrailSlope;	// stop loss adaption factor, 0..1
	float	fTrailStep;	// stop loss bar step factor, 0..1
	float	fSpread;		// spread at entry for short, zero for long trades
	float	fMAE,fMFE;	// Max adverse excursion, max favorite excursion without slippage, spread etc.
	float fRoll;		// accumulated rollover, < 0
	float fSlippage;		// Slippage, < 0
	float	fUnits;		// conversion factor from asset price to account money
	float	fTrailSpeed;	// break even speed factor 
	int	nExitTime;	// sell at market after this number of bars
	int	nEntryTime;	// wait this number of bars for entry limit orders
	int	nLots;		// number of Lots. fUnits = nLots * vPIPCost/vPIP.
	int	nBarOpen;	// bar number of order and entry
	int	nBarClose;	// current bar number while open, otherwise exit bar number
	int	nID;			// active trade id, or 0 for pending trades; can change when trades are partially closed
	DATE	tEntryDate;	// entry time target
	DATE	tExitDate;	// exit time target
	DWORD flags;		// trade flags, see below
	float	fArg[8];		// trade management arguments
	var	Skill[NUM_SKILLS];	// general purpose variables for trade micromanagement 
// saved until this element
	struct TRADE *un;	// scenario specific trade list / not used
	void	*status;		// trade STATUS 
	function manage;	// trade management function pointer
	float	fSignal[NUM_SIGNALS]; // advisor parameters
	float fLastStop;	// for comparing the stop
	float fProfit;		// simulated profit, externally set 
} TRADE;

#define TR_BID			(1<<0)	// short position
#define TR_OPEN		(1<<1)	// position is open
#define TR_NOTFOUND	(1<<2)	// trade disappeared from the broker list
#define TR_WAITSELL	(1<<4)	// close position at the next tick
#define TR_WAITBUY	(1<<5)	// open position at the next tick
#define TR_DETREND	(1<<6)	// detrend the trade result
#define TR_SUSPEND	(1<<7)	// suspend trade function
#define TR_EVENT		(1<<8)	// trade function was called by enter/exit event
#define TR_IGNORE		(1<<9)	// don't automatically enter/exit
#define TR_MISSEDENTRY	(1<<10)	// missed the entry limit or stop, or sell price in the last bar
#define TR_MISSEDEXIT	(1<<11)	// missed the exit for some reason
#define TR_NOSIZE		(1<<12)	// Trade not executed, not enough lots or balance
#define TR_PREDICT	(1<<13)	// Use trade result for advise()
#define TR_NONET		(1<<14)	// Don't open a net trade yet
#define TR_NET			(1<<15)	// Virtual hedging
#define TR_PHANTOM	(1<<16)	// Trade for status history only; does not change equity
#define TR_NOW			(1<<17)	// position just opened in the last bar
#define TR_STOPPED	(1<<18)	// closed due to stop loss in the last bar, or a margin call
#define TR_PROFIT		(1<<19)	// closed due to profit target in the last bar
#define TR_TIME		(1<<20)	// closed due to timeout in the last bar
#define TR_SOLD		(1<<21)	// closed due to exit at market in the last bar
#define TR_TRAILED	(1<<22)	// trail message on this bar
#define TR_WIN			(1<<23)	// plot this trade green in the price chart
#define TR_LOSE		(1<<24)	// plot this trade red in the price chart
#define TR_ENTRYSTOP	(1<<25)	// entry stop, rather than limit
#define TR_ENTER		(1<<26)  // entered by TMF
#define TR_EXIT		(1<<27)  // exit by TMF
#define TR_REMOVED	(1<<28)	// removed from the online trade list (f.i. margin call or manually closed)
#define TR_BAR			(1<<29)	// run TMF on any bar only, not any tick
#define TR_REVERSED	(1<<30)  // indicate exit by reversal (shared with TR_EXIT)

///////////////////////////////////////////////////////////////
// trade specific performance statistics (per lot)
typedef struct STATUS { 
	var	vSkippedMargin;	// margin accumulation of skipped trades
	var	vWin,vLoss;			// gross wins and losses
	var	vWinVal,vLossVal;	// value of open trades, test/trade mode only
	var	vReturn2;			// sum of squared returns
	var	vWinMax,vLossMax;	// largest win and loss
	var	vLossLotMax;		// largest loss per lot
	var	vWinStreakVal,vLossStreakVal;	// size of current win/loss streak
	int	numWin,numLoss;	// number of won and lost trades	
	int	nWinStreak,nLossStreak; // length of current win and loss streak
	int	numWinning,numLosing;	// number of winning and losing open trades, test/trade mode only
	DWORD	dwWin,dwLoss;		// WFO win/loss flags 
	DWORD	dwColorWin,dwColorLoss;	// trade colors in chart
// clear statistics until here
	DWORD	flags;
	int	nModel;				// model number for prediction
	int	nComponent;			// component number
	DWORD	pad[1];
	var	vPad[4];
	var	Skill[NUM_SKILLS];	// general purpose variables for money managemement
	var	Result[NUM_RESULTS];	// last 20 trade results
	char	sAlgo[NAMESIZE];	// algo identifier
// save until here
	var	vTrainPF;			// training profit factor
	var	vOptimalF;			// component reinvestment factor, long/short
	var	vOptimalF2;			// OptimalF (short), R2 (long)
	var	vWFOProfit;			// profit of the current WFO cycle
	int	numPending;			// number of pending trades
	int	nBalance;			// net sign sum of advise objective
	int	numSignals;			// signals per rule
	void*	History;				// signal history for rule learning
	void*	Rule;					// pointer to tree or perceptron function
// clear again until here
	struct STATUS *other;	// other status (short<->long)					
	ASSET	*asset;				// asset pointer
	int	nCycles;				// number of optimize cycles
	int	nSteps[MAX_PARAMS];	// list of optimize steps
	float	fParam[MAX_PARAMS];	// list of optimal parameters
	float* fStat;				// matrix of optimize results
	var*	pCurve;				// component equity/balance curve
} STATUS;

// overall performance statistics
typedef struct PERFORMANCE
{
	var	vWin,vLoss;			// gross wins and losses
	var	vPips;				// gross win-loss in pips
	var	vWinMax,vLossMax;	// largest win and loss
	var	vDrawDown;			// maximum balance-equity drawdown 
	var	vEquityDown;		// max equity drawdown
	var	vCommissionCost;	// accumulated negative commissions
	var	vPhantomWin,vPhantomLoss;	// win/loss by virtual trades
	var	vMarginMax;			// maximum margin 
	var	vRiskMax;			// maximum risk 
	var	vVolume;				// total trade volume
	var	vObjective;			// result of objective function
	var	vR2;					// coefficient of determination
	var	vMean;				// average simple return per bar
	var	vStdDev;				// standard deviation of simple returns
	var	vUlcer;				// ulcer index; root of mean of squared drawdown percentages
	var	vRollCost;			// accumulated rollover costs
	var	vSlippageWin,vSlippageLoss;		// accumulated slippage costs
	var	vSpreadCost;		// accumulated spread costs
	int	numWin,numLoss;	// number of winning and losing trades 
	int	numWinStreakMax;	// max number of consecutive wins
	int	numLossStreakMax;	// max number of consecutive losses
	int	numMarketBars;		// number of bars with active trading
	int	numDrawDownBars;	// number of bars in balance drawdown
	int	numMAEBars;			// number of bars in equity drawdown
	int	numDrawDownMax;	// max drawdown bars
	int	numMarketWin,numMarketLoss;	// bars in market of winning/losing trades
	int	numMarketTotal;	// total number of bars in market
	int	numTradeBarsMax;	// max length of trade
	long	pad[16];
} PERFORMANCE;

// global variables
typedef struct GLOBALS
{
// trade parameters (r/w)
	var	vLots;	// number of lots to buy; 1 lot = minimum order amount = vPIPCost/vPIP contracts
	var	vMargin;	// margin per trade; determines lots when nonzero
	var	vRisk;	// risk limit per trade; limits lots according to from vStop
	var	vCapital; // initial capital for reinvesting strategies
	var	vStop;	// stop loss - sell when the trade loses this amount 
	var	vTrail;	// adapt stop loss when the trade wins this amount 
	var	vTrailSlope;	// move stop loss by this percentage of current trade profit
	var	vTrailLock;	// lock this percentage of favorite excursion
	var	vTrailStep;	// move stop loss every bar by this percentage of price-stop distance
	var	vTakeProfit; // profit limit - sell when the trade goes your way by this amount 
	var	vEntry;	// order entry - buy when the price rises or falls by this amount 
	var	vEntryDelay;
	int	nExitTime;	// sell at market after this number of bars
	int	nEntryTime;	// wait this number of bars for entry limit orders
	
	int	nHedge;		// hedging mode
	int	nVerbose;	// verbosity level
	int	nBoxState;	// external message box state
	int	nLifeTime;	// trade duration in bars			

	var	vSlippage;  // simulated order execution delay in seconds (default = 0.5)
	var	vBarPeriod;	// bar period in minutes, fractional
	
	var	vRound;	// rounding factor, i.e 0.1 = round to the first decimal
	var	vFuzzyRange;	// for fuzzy logic
	var	vFuzzyLevel;	// for defuzzy

	int	nGapDays;		// gap tolerance
	int	nPad01;
	int	nMonteCarlo;	// Monte Carlo iterations
	int	nConfidence;	// Monte Carlo confidence level
	var	vMCDrawDown;	// Drawdown at confidence level

	var	vStopFactor;	// for sending the stop to the broker
	var	vPriceStep;		// price step for stop/target/entry
	var	vTrailSpeed;	// trail weight factor in percent
	var	vAssetFactor,vAssetOffset;
	int	numCores;		// multi-core training
	int	nCore;			// current core, 1..numCores
	var	vAdvise[2];		// advise parameters
	int	nOptimize;		// optimize mode
	int	nFill;			// order fill mode
	int	nTickSize;		// T6 or THL

	long	pad1[7];

// simulation performance (r/o)
	STATUS	*statLong,*statShort;	// component statistics, set up by asset() and algo()
	PERFORMANCE	w;			// overall win and loss statistics
	int	numTradesMax;	// max number of concurrent trades
	var	vBalancePeak;	// current balance peak
	var	vEquityPeak,vEquityValley;	// current equity peak and subsequent valley
	var	vEquityMin;		// not used
	int	nBalancePeakBar;	// and their bar numbers
	int	nEquityPeakBar,nEquityValleyBar;	
	int	nDnMaxBar;			// bar number of the longest down time 
	int	nWinStreak,nLossStreak;	// number of consecutive wins / losses
	var	vWinStreakVal,vLossStreakVal;	// size of current win/loss streak
	var	vMarginAvg;		// average margin
	long	pad11[2];

// account situation (r/o)
	var	vMarginSum; // total margin required for all open positions
	var	vRiskSum;	// total risk of all open positions
	var	vWinVal,vLossVal;	// current win/loss of all open positions
	var	vBalance;	// account balance in trade mode
	var	vEquity;		// account balance plus open trade value in trade mode
	int	numTrades;	// number of open or pending trades
	int	numPending;	// number of virtual or real pending trades
	int	numShort,numLong;	// number of open long or short positions
	int	numWinning,numLosing,numEven;	// # of trades winning, losing, break even
	int	numWFOWin,numWFOLoss;	// number of won or lost WFO cycles

// simulation/optimization parameters (r/w)
	int	nBarZone;		// DST zone for local time bars
	int	nBarOffset;	// bar period offset in minutes
	int	nTimeFrame;	// time frame for price() and series() in number of bars
	int	nFrameOffset;	// timeframe offset in bars
	int	numTotalCycles;	// number of complete cycles
	int	numSampleCycles;	// number of oversampling cycles 
	int	numOptCycles;	// number of parameter optimize runs
	int	numWFOCycles;	// number of WFO runs
	int	numYears;	// number of years of the simulation (default = 0 = all years up to last year)
	int	numDays;		// number of days of the simulation (default = 0 = all)
	int	nStartDate;	// simulation start year (yyyy) or exact start date (yyyymmdd] (default = 2010)
	int	nEndDate;	// simulation end date (yyyymmdd] (default = current year)
	int	nStartWeek,nEndWeek;		// Week start and end day & hour
	int	nUpdateDays;	// automatically update history when older than given number of days (default: 60)
	int	nMaxBars;	// the maximum number of bars to simulate
	int	nUnstablePeriod;	// number of lookback bars added for exponential indicators (default = 40)
	int	nLookBack;	// number of bars before the simulation starts (automatically adjusted)
	int	nTickTime;	// minimum tick time in milliseconds (default = 100)
	int	nTickAvg;	// Price smoothing on incoming prices
	int	nDataSplit;	// Percentage of in sample data (f.i. 90 for 90% in sample, 10% out of sample)
	int	nDataSkip;	// Bars to skip with the SKIP1..3 flag
	int	nDetrend;	// 1 = detrend trade results; 2 = detrend price functions; 4 = detrend price curve
	int	nWeekend;	// 1 = don't trade during weekend (default); 2 = don't manage trades during weekend; 4 = log out during weekend
	int	nTradesPerBar;	// max trades / numbars
	var	vDataSlope;	// 1..3, bias factor for giving more weight to the last trades
	var	vDataWeight; // automatically set, 3 or 1.5 for one-week and two-week data sets
	var	dOptFactor[2]; // Optimize factors at range borders
	var	vOptimalFRatio; // 0 or 2..10, ratio of maximum to minimum OptimalF
	float fWeight;		// current weight factor
	int	nTimeStep;	// last time frame
	int	nWFOPeriod;	// length of the WFO period
	int	nWFOMethod;	
	int	nDay;			// current simulation day (after lookback)
	int	numAllocDays;	// maximum number of days in the simulation
	int	pad12;
	DATE	tStartTime;		// session start time
	int	nReTrainDays;	// re-training interval
	int	nCommand[4];	// command line numbers

	long	pad2[7];

	var	vSlider[4]; // current values from the sliders, in the range defined by slider()
	int	nProgress1,nProgress2;	// progress bar
	string sInfo;						// info window

// simulation state (r/o)
	int	nFirstBar;	// first bar that lies at or after g->nStartDate
	int	numBars;		// total number of bars of the simulation, depends on nBarPeriod
	int	numAllocBars;	// size of the bar, plot, and price arrays
	int	numMinutes;	// total minutes of the test frame	
	int	nTick;		// current tick number in trade management functions
	int	nBar;			// current bar number
	int	nTotalCycle;	// current cycle
	int	nSampleCycle;	// current oversampling cycle number	
	int	nParLoop;	// current optimize call number within one loop
	int	nParTotal;	// current optimize call number of all loops
	int	numParTrain;	// number of parameters trained per loop
	int	nStepCycle;	// current optimize step
	int	nParCycle;	// current parameter optimizing run, 1..numParTrain
	int	nOptCycle;	// when parameters are optimized twice
	int	nWFOCycle;	// current WFO cycle
	int	nFrameStart;	// start bar of the active data frame
	int	numFrameBars;	// size of the active data frame in bars
	int	numLoops[2];	// total number of loops in training mode
	int	nLoop[2];		// current loop argument number (starting with 1)
	int	nLoopCycle[2];	// trained loop argument number, or 0 for whole loop
	void*	pLoopPar[2];	// current loop parameter
	int	nSelectWFO;		// WFO cycle to select, or 0 for all cycles
	int	nLogCycle;		// Log trades of a certain train cycle; format WLLPS (Walk Loop0 Loop1 Par Step)
	BOOL	bDoStep,bDoLoop;	// TRUE during optimization
	int	numAssets;		// number of assets used in the script
	var	vParameter;		// current optimize value
	var	vProfitMax,vProfitMin;	// maximum and minimum result of all bar cycles
	int	nWFOStart;			// bar number of the current WFO cycle start
	int	nTrainFrame,nTestFrame;		// size of the training/test period in bars
	int	nModels;			// number of models for prediction
	int	nUserBar;		// return value from bar() function
	BOOL	bFor;				// inside a for(trade) loop
	int	numPhantom;		// number of phantom trades
	int	numRejected;	// number of rejected trades
	int	nComponents;	// component counter

	long	pad3[5];

	int	nLastPeriod;	// last time period passed to an indicator
	var	*pLastSeries;	// last series passed to an indicator
	var	*pTime;		// array of internal time measurements
	var	*pCurve;		// daily balance/equity curve
	long	RHandle;		// handle of the R Bridge
	string sParameters; // content of the parameter file
	string sFactors;	// content of the OptimalF factor file
	string sRules;		// content of the rule file
	var	*pParameters;	// array of parameters from the optimize loop
	var	*pParLoop;	// parameter list in current loop
	void	*pRules;		// compiled advise rules

	TRADE	*tr;			// current trade in trade function or enumeration
	ASSET	*asset;		// current asset, set up by asset()
	BAR	*bars;		// list of bars
	float	*pResults;	// array of bar results
	var	*pEquity;	// displayed equity curve
	var	*pDrawDown;	// displayed drawdown curve
	var	*pMonteCarlo;	// Monte Carlo simulation results

	string sAlgo;		// current algorithm identifier, set up by algo()
	string sScript;	// current script name, used for .par/.fac/.c
	string sExeName;	// current name for the .x file to compile
	string sAssetList;	// file name of current assets list
	string sCSVLogName;	// CSV log file path for storing trade results
	string sBroker;	// name returned by the broker plugin
	string sHistory;	// extension of the history files
	string sWebFolder;	// HTML page folder
	string sFactorList;	// factor file name
	string sZorroFolder;	// Zorro's own folder
	string sRTermPath;	// path to the R terminal
	string sCurves;	// file name for daily balance curves
	string *pAssets;	// list of asset names
	string sMessage;	// last critical message f.i. for possible orphans
	string sAccount;	// Account name from the scrollbox

	long	pad4[23];

// chart/log parameters (r/w)
	int	nPlotScale;	// width of a bar in the chart image, in pixels (default = 4; 0 for no chart)
	int	nPlotWidth;	// maximum main chart width in pixels
	int	nPlotHeight1,nPlotHeight2;	// Main chart and additional chart height in pixels
	int	nPlotDate;	// start date of the plot (default = 0 = plot all)
	int	nPlotBars;	// number of bars to plot (default = 0 = all)
	DWORD	dwColorCandle;
	DWORD dwColorUp,dwColorDn;	// color of white / black candles
	DWORD	dwColorEquity,dwColorDD;
	DWORD	dwColorWin,dwColorLoss;
	string sPlotLabelFormat;
	int	nPlotTrade;
	DWORD	dwColorBars[3];
	DWORD	dwColorPanel[6];

	long	pad5[17];
	DWORD nDiagMode;	// 1, 2, 3..
	DWORD	flags2;		// more internal flags

	DWORD	nSaveMode;	// load/save flags
	DWORD	dwBrokerPatch;	// work around API bugs
	DWORD	dwMode;		// mode switches
	DWORD dwStatus;	// status flags
	int	nState;		// training machine state
	DWORD	flags;		// internal flags
	void	*Functions;	// null terminated function list
} GLOBALS;

#define SCRIPT_VERSION	144

// mode switches
#define SKIP1		(1<<0)	// skip 1st of every 3 weeks
#define SKIP2		(1<<1)	// skip 2nd of every 3 weeks
#define SKIP3		(1<<2)	// skip 3rd of every 3 weeks
#define BINARY		(1<<3)	// trade binary options
#define PRELOAD	(1<<4)	// load prices from historical data
#define PLOTNOW	(1<<5)	// create a chart automatically after test
#define EXTRADATA	(1<<6)	// use historical volume & spread data
#define PLOTLONG	(1<<7)	// start plot already with lookback period
#define LOGFILE	(1<<8)	// store log file
#define LOGMSG		(1<<9)	// show log in message window
#define EXE			(1<<11)	// generate EXE (Zorro S)
#define RULES		(1<<12)	// generate/use advise rules
#define FACTORS	(1<<13)	// generate reinvestment factors
#define PARAMETERS (1<<14)	// generate/use strategy parameters
#define OPENEND	(1<<15)	// ignore open trades at the end of the test
#define PEEK		(1<<16)	// allow peeking into the future
#define RISKLIMIT	(1<<17)	// don't trade when trade risk > 2*Risk
#define MARGINLIMIT (1<<18) // don't trade when real margin > 2*calculated margin, or when total margin left < 1000
#define ACCUMULATE (1<<19)	// accumulate Margin for skipped trades
#define TESTNOW	(1<<20)	// run a test automatically after training
#define RECALCULATE (1<<21) // recreate series after parameter loading
#define SCREENSAVER (1<<22) // don't suspend power management 
#define NAIVE		(1<<23)	// no slippage; entry/exit at current price
//#define FAST		(1<<24)	// ticks in FAST mode
#define NFA			(1<<25)	// NFA compliant account: no "hard" stop loss, no hedging, no position closing
#define ATCLOSE	(1<<26)	// special sell mode (at the close price of the bar)
#define TICKS		(1<<27)	// simulate trades every tick (slow)
#define BALANCE	(1<<28)	// store and display balance rather than equity curves
#define STEPWISE	(1<<29)	// stepwise debugging (Zorro S)
#define ALLCYCLES	(1<<30)	// sum up statistics over all sample cycles

// status flags
#define TRADING	(1<<0)	// trades have been opened
#define CHANGED	(1<<1)	// script or asset was changed -> init strategy sliders
#define INITRUN	(1<<2)	// init run before the first bar, for initialization
#define EXITRUN	(1<<3)	// last bar, all trades are closed, for result calculation
#define TESTMODE	(1<<4)	// [Test] mode
#define TRAINMODE	(1<<5)	// [Train] mode, for optimizing
#define TRADEMODE	(1<<6)	// [Trade] mode
#define DEMO		(1<<7)	// Running on demo account
#define LOOKBACK	(1<<8)	// Lookback period, no trading
#define FIRSTRUN	(1<<9)	// First run with valid price data, usually on bar 1
#define COMMAND	(1<<10)	// Zorro started from the command line
//#define EXE		(1<<11)		see above, script is executable (*.x)
//#define RULES	(1<<12)	// generate/use advise rules
//#define FACTORS	(1<<13)	// generate reinvestment factors
//#define PARAMETERS (1<<14)	// generate/use strategy parameters
#define PORTFOLIO	(1<<16)	// script contains one or several loop calls
#define ASSETS		(1<<17)	// asset() function called
#define SELECTED	(1<<18)	// asset is same as [Asset] Scrollbox (not in loops)
#define PLOTSTATS	(1<<19)	// plot histogram rather than price chart
#define AFFIRMED	(1<<20)	// [Ok] clicked on nonmodal message box
#define SPECIALBAR (1<<21)	// user-defined bar length
#define MARGINCALL (1<<22)	// Margin + Loss exceeds Capital
#define NEWDAY		(1<<23)	// Day change after last bar 
#define PROCESS	(1<<24)	// ReTrain or ReTest
#define SPONSORED	(1<<25)	// Zorro S version
#define RUNNING	(1<<26)	// Simulation is running
#define FIRSTINITRUN (1<<27)	// Really first run
#define SHORTING	(1<<28)	// Short trades have been opened

/////////////////////////////////////////////////////////

#endif
