
import java.util.Random;

//###################################################################
// ゲームの盤面
class MsBoard {
	private int width;     // 幅
	private int height;    // 高さ
	private int numBombs;  // 爆弾の個数

	private int[][] currentCells;   // 現在の盤面(未オープン=-1)
	private int[][] solvedCells;    // 正解の盤面(爆弾=9)
	private int numOpenedCells = 0; // オープンされたマスの個数
	private int numFlags       = 0; // 置かれたフラグの個数

	// 爆弾を置いたかどうかを表すフラグ
	/*
	 * 初手にマスを開こうとしたかフラグを立てようとしたときに
	 * 実際の爆弾の配置を定める．初手にマスを開く場合には，
	 * そのマスに爆弾が配置されないようにする
	 */
	private boolean hasPutBombs = false;

	private boolean isValidPlay = false; // 有効なプレイであったか
	private int numPlayed       = 0;     // ターン内のプレイ回数
	private int openedValue;             // そのターンで開いた数値
	private int lastPlayScore;           // 最後の操作のスコア

	// ゲームの経過を表示するかどうかをあらわすフラグ
	private boolean isMsgShown = true;

	//***************************************************************
	// コンストラクタ
	public MsBoard(int width, int height, int numBombs) {
		initialize(width, height, numBombs);
	}

	public MsBoard(int level) {
		switch (level) {
		case 1:  initialize( 9,  9, 10); break; // 初級
		case 2:	 initialize(16, 16, 40); break; // 中級
		case 3:  initialize(30, 16, 99); break; // 上級
		default: initialize( 9,  9, 10); break;
		}
	}

	private void initialize(int width, int height, int numBombs) {
		this.width         = width;
		this.height        = height;
		this.numBombs      = numBombs;
		this.lastPlayScore = 0;

		if (numBombs>width*height-1) {
			System.out.println
			("numBombs must be less than width*height.");
			this.numBombs = width*height-1;
		}

		// 正解の盤面を保持する配列の実体を作成
		solvedCells = new int[height][width];

		// 現在の盤面の初期化
		// 添え字は[行番号/height/y][列番号/width/x]の順
		currentCells = new int[height][width];
		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				currentCells[y][x] = -1; //未オープン
			}
		}
	}

	//***************************************************************
	// 爆弾の配置を決定する
	private void locateBombs(int x, int y) {
		//-----------------------------------------------------------
		/* Fisher-Yatesのアルゴリズムを用いて爆弾の位置を決定
		 * 配列orderをシャッフルする(numBombs+1個の乱数を取り出せる)
		 *
		 * i を 0 から numBombs まで増加させながら以下を実行する
		 *  - j に i 以上 order.length 未満のランダムな整数を代入する
		 *  - order[j] と order[i] を交換する
		 */
		Random rand = new Random();
		int[] order = new int[height*width];
		for (int i=0; i<height*width; i++) {
			order[i] = i;
		}
		for (int i=0; i<=numBombs; i++) {
			int j = i + rand.nextInt(order.length-i);
			int tmp  = order[j];
			order[j] = order[i];
			order[i] = tmp;
		}

		//-----------------------------------------------------------
		// 爆弾の配置
		int nb=0; // 置かれた爆弾の数

		for (int i=0; true; i++) {
			// 無限ループで処理
			// nbが指定されたnumBombsに等しくなったらbreak
			int r = order[i];
			int bx = r % width;
			int by = r / width;
			// オープンされたマスには爆弾を置かない
			if (bx==x && by==y) {
				continue;
			}
			solvedCells[by][bx] = 9;
			nb++;
			if (nb==numBombs) {
				break;
			}
		}
	}

	//***************************************************************
	// 爆弾の個数をカウントする
	private void countBombs() {
		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				if (solvedCells[y][x]==9) {
					continue;
				} else {
					// ネストが深くなるので別メソッドに分ける
					solvedCells[y][x] = countBombs(x, y);
				}
			}
		}
	}

	//***************************************************************
	// 位置(x, y)のマスの周囲の爆弾の数を返す
	private int countBombs(int x, int y) {
		int numLocalBombs = 0;
		for (int yOffset=-1; yOffset<=1; yOffset++) {
			for (int xOffset=-1; xOffset<=1; xOffset++) {
				if (!isValidNeighbor(x, xOffset, y, yOffset)) {
					continue;
				}
				if (solvedCells[y+yOffset][x+xOffset]==9) {
					numLocalBombs++;
				}
			}
		}
		return numLocalBombs;
	}

	//***************************************************************
	// 位置(x, y)のマスをオープンする
	public boolean openCell(int x, int y) {
		isValidPlay = false;

		// 開けるマスかどうか判定
		if (x<0 || x>=width
			|| y<0 || y>=height
			|| currentCells[y][x]!=-1) {
			if (isMsgShown) {
				System.out.println
				("- cannot open cell (" + x + ", " + y + ")");
			}
			return false;
		}
		numPlayed++;
		isValidPlay = true;

		// openCellを連鎖的に呼び指す際に最初の1回だけメッセージを
		// 表示させたかったので，中身の処理を別メソッドにまとめて
		// メッセージの有無を第3引数で指定できるようにした
		openCell(x, y, true);

		//-----------------------------------------------------------
		// 結果の表示と得点の計算
		int sum = calcSumPeriphOpenedCells(x, y);
		if (currentCells[y][x] == 9) {
			if (sum==0) {
				lastPlayScore = -10;
				System.out.println
				("BOMB (" + lastPlayScore + ")");
			}
			else {
				lastPlayScore = sum * (-10);
				System.out.println
				("BOMB (" + sum + "*" + "-10)");
			}
		}
		else {
			sum += currentCells[y][x];
			int r = getSuccessRatio(x, y);
			lastPlayScore = sum * r;
			System.out.println("safe (" + sum + "*" + r + ")");
		}

		// 状態表示
		if (isMsgShown==true) {
			showCurrentBoard();
		}

		return true;
	}

	//***************************************************************
	// 位置(x, y)のマスをオープンする
	private boolean openCell(int x, int y, boolean showsMessage) {
		// メッセージの表示
		if (showsMessage==true) {
			System.out.println("- opened cell (" + x + ", " + y + ")");
		}

		//-----------------------------------------------------------
		// 最初のマスのオープン時に正解の盤面を決定する
		// このとき，最初のマスは必ず爆弾にならないよう調整する
		if (!hasPutBombs) {
			locateBombs(x, y);
			countBombs();
			hasPutBombs = true;
		}

		//-----------------------------------------------------------
		// マスをオープンしてsolvedCellsからcurrentCellsに値をコピー
		numOpenedCells++;
		currentCells[y][x] = solvedCells[y][x];

		//-----------------------------------------------------------
		// 0をオープンした場合は周囲のマスも連鎖的にオープン
		if (currentCells[y][x] == 0) {
			for (int yOffset=-1; yOffset<=1; yOffset++) {
				for (int xOffset=-1; xOffset<=1; xOffset++) {
					if (!isValidNeighbor(x, xOffset, y, yOffset)) {
						continue;
					}

					// オープンされていない周囲のマスをオープン
					if (currentCells[y+yOffset][x+xOffset]==-1) {
						openCell(x+xOffset, y+yOffset, false);
					}
				}
			}
		}

		return true;
	}

	//***************************************************************
	// 位置 (x, y) のマスにフラグを立てる
	public boolean putFlag(int x, int y) {
		isValidPlay = false;

		// フラグを立てられるマスかどうか判定
		// 開けるマスかどうか判定
		if (x<0 || x>=width
			|| y<0 || y>=height
			|| currentCells[y][x]!=-1) {
			if (isMsgShown) {
				System.out.println
				("- cannot put flag (" + x + ", " + y + ")");
			}
			return false;
		}
		numPlayed++;
		isValidPlay = true;

		//-----------------------------------------------------------
		// 最初のマスのオープン時に正解の盤面を決定する
		// このとき，最初のマスは必ず爆弾にならないよう調整する
		if (!hasPutBombs) {
			locateBombs(x, y);
			countBombs();
			hasPutBombs = true;
		}

		//-----------------------------------------------------------
		// 結果の表示と得点の計算
		lastPlayScore = 0;
		int sum = calcSumPeriphOpenedCells(x, y);
		if (solvedCells[y][x]!=9) {
			if (sum==0) {
				lastPlayScore = -100;
				if (isMsgShown==true) {
					System.out.println
					("- put flag (" + x + ", " + y + ") ... miss ("
							+ "-100)");
				}
			}
			else {
				int r = getFlagMissRatio(x, y);
				lastPlayScore = sum * r;
				if (isMsgShown==true) {
					System.out.println
					("- put flag (" + x + ", " + y + ") ... miss ("
							+ sum + "*" + r + ")");
				}
			}
		}
		else {
			currentCells[y][x] = 10;
			numFlags++;
			if (sum==0) {
				lastPlayScore = 50;
				if (isMsgShown==true) {
					System.out.println
					("- put flag (" + x + ", " + y + ") ... success ("
							+ "50)");
				}
			}
			else {
				int r = getSuccessRatio(x, y);
				lastPlayScore = sum * r;
				if (isMsgShown==true) {
					System.out.println
					("- put flag (" + x + ", " + y + ") ... success ("
							+ sum + "*" + r + ")");
				}
			}
		}

		//-----------------------------------------------------------
		// 状態表示
		if (isMsgShown==true) {
			showCurrentBoard();
		}

		return true;
	}

	//***************************************************************
	// 位置 (x, y) のマスの近傍でオープンされたマスの合計を求める
	private int calcSumPeriphOpenedCells(int x, int y)
	{
		int sum = 0;
		for (int yOffset=-1; yOffset<=1; yOffset++) {
			for (int xOffset=-1; xOffset<=1; xOffset++) {
				if (!isValidNeighbor(x, xOffset, y, yOffset)) {
					continue;
				}
				if (currentCells[y+yOffset][x+xOffset] >= 0
					&& currentCells[y+yOffset][x+xOffset] <= 8) {
					sum += currentCells[y+yOffset][x+xOffset];
				}
			}
		}
		return sum;
	}

	//***************************************************************
	// 位置 (x, y) の周囲のオープンされたセルで最大の値を求める
	private int getMaxPeriphOpenedCell(int x, int y)
	{
		int max = 0;
		for (int yOffset=-1; yOffset<=1; yOffset++) {
			for (int xOffset=-1; xOffset<=1; xOffset++) {
				if (!isValidNeighbor(x, xOffset, y, yOffset)) {
					continue;
				}
				if (currentCells[y+yOffset][x+xOffset] >= 0
					&& currentCells[y+yOffset][x+xOffset] <= 8
					&& currentCells[y+yOffset][x+xOffset]>max) {
					max = currentCells[y+yOffset][x+xOffset];
				}
			}
		}
		return max;
	}

	//***************************************************************
	// 位置 (x, y) の安全マスオープン，フラグ立てに成功した際の倍率
	private int getSuccessRatio(int x, int y) {
		int max   = getMaxPeriphOpenedCell(x, y);
		switch (max) {
		case 0:
		case 1:
		case 2:
		case 3:
		case 4:
			return 1;
		case 5:
			return 10;
		case 6:
			return 20;
		case 7:
			return 50;
		case 8:
			return 100;
		default:
		}
		return 0;
	}

	//***************************************************************
	// 位置(x, y)のフラグ立てに失敗した際の倍率を求める
	private int getFlagMissRatio(int x, int y) {
		int max   = getMaxPeriphOpenedCell(x, y);
		switch (max) {
		case 0:
		case 1:
		case 2:
		case 3:
		case 4:
			return -10;
		case 5:
			return -20;
		case 6:
			return -30;
		case 7:
			return -40;
		default:
		}
		return 0;
	}

	//***************************************************************
	// 位置 (x, y) のマスの状態を返す
	public int getCell(int x, int y) {
		return currentCells[y][x];
	}

	//***************************************************************
	// (x+xOffset, y+yOffset)が有効な近傍マスであるかどうかを返す
	private boolean isValidNeighbor(int x, int xOffset,
									int y, int yOffset) {
		if (x+xOffset<0 || x+xOffset>=width
			|| y+yOffset<0 || y+yOffset>=height) {
			// 盤面の外
			return false;
		} else if (xOffset==0 && yOffset==0) {
			// (x, y)と同一マス＝近傍マスではない
			return false;
		}
		return true;
	}

	//***************************************************************
	// クリア状態かどうか判定する
	public boolean isCleared() {
		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				// 爆弾でない未オープンなマスがあるかどうか
				if (currentCells[y][x]==-1
					&& solvedCells[y][x]!=9) {
					return false;
				}
			}
		}
		return true;
	}

	//***************************************************************
	// ゲームオーバーかどうか判定する
	public boolean isGameOver() {
		if (numOpenedCells + numFlags == height * width)
		{
			// 正解の盤面を表示する
			// showSolvedBoard();
			return true;
		}
		return false;
	}

	//***************************************************************
	// 状態をリセットする
	public void resetStatus()
	{
		isValidPlay = false;
		numPlayed   = 0;
		openedValue = 0;
	}

	//***************************************************************
	// 現在の盤面を表示する
	public void showCurrentBoard() {
		// showCurrentBoardとshowSolvedBoardはほぼ同じ処理であり，
		// 対象とする配列のみが異なるため，対象とする配列を引数として
		// 表示部本体をshowCellsへ括り出した
		showCells(currentCells);
	}

	//***************************************************************
	// 正解の盤面を表示する
	public void showSolvedBoard() {
		showCells(solvedCells);
	}

	//***************************************************************
	// 盤面を表示するメソッドの本体
	private void showCells(int[][] cells) {
		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				switch (cells[y][x]) {
				case -1:
					System.out.print(".");
					break;
				case 9:
					System.out.print("*");
					break;
				case 10:
					System.out.print("F");
					break;
				default:
					System.out.print(cells[y][x]);
					break;
				}
				System.out.print(" ");
			}
			System.out.println();
		}
		System.out.println();
	}

	//***************************************************************
	// アクセッサ
	public int getWidth()          { return width; }
	public int getHeight()         { return height; }
	public int getNumBombs()       { return numBombs; }
	public int getNumOpenedCells() { return numOpenedCells; }

	public boolean isValidPlay()  { return isValidPlay; }
	public int getNumPlayed()     { return numPlayed; }
	public int getOpenedValue()   { return openedValue; }
	public int getLastPlayScore() { return lastPlayScore; }

	public void setMsgShown(boolean flag) {	isMsgShown = flag; }
	public boolean isMsgShown()           { return isMsgShown; }
}