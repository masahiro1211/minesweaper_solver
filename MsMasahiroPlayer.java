import java.util.ArrayList;
import java.util.List;
public class MsMasahiroPlayer extends MsPlayer {
    //***************************************************************
	// コンストラクタ
	public MsMasahiroPlayer(){ super(); }
	public MsMasahiroPlayer(MsBoard board){ super(board); }

	//***************************************************************

    //x,yが周囲に開示済みマス(9,10除く)があるかどうかを調べる関数
    private boolean hasRevealedNeighbor(int x, int y,int [][] current_board){
        int height = board.getHeight();
        int width = board.getWidth();
        int[] dx = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] dy = {-1, 0, 1, -1, 1, -1, 0, 1};
        for (int i = 0; i < 8; i++) {
                int nearx = x + dx[i];
                int neary = y + dy[i];

                if (nearx >= 0 && nearx < width && neary >= 0 && neary < height && 0<current_board[neary][nearx] && current_board[neary][nearx]<=8) {
                    return true;
                }
            }
        return false;
    }
    //x,yが周囲に非開示マスがあるかどうかを調べる関数
    private boolean hasUnrevealedNeighbor(int x, int y, int[][] current_board) {
        int height = board.getHeight();
        int width = board.getWidth();
        int[] dx = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] dy = {-1, 0, 1, -1, 1, -1, 0, 1};
        
        for (int i = 0; i < 8; i++) {
            int nearx = x + dx[i];
            int neary = y + dy[i];

            if (nearx >= 0 && nearx < width && neary >= 0 && neary < height && (current_board[neary][nearx] == -1 ) ) {
                return true;
            }
        }
        return false;
    }

    //開いている隣接マスの数字を合計したもの、基本スコア
    private int sumAdjacentNumbers(int x, int y, int[][] current_board) {
        int height = board.getHeight();
        int width = board.getWidth();
        int[] dx = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] dy = {-1, 0, 1, -1, 1, -1, 0, 1};
        int sum = 0;
    
        for (int i = 0; i < 8; i++) {
            int nearx = x + dx[i];
            int neary = y + dy[i];
    
            if (nearx >= 0 && nearx < width && neary >= 0 && neary < height && 0<current_board[neary][nearx] && 9>current_board[neary][nearx]) {
                sum += current_board[neary][nearx];
            }
        }
        return sum;
    }

    //隣接マスの最大の数字からもとめたもの、倍率
    private int[] maxAdjacentNumber(int x, int y, int[][] current_board) {
        int height = board.getHeight();
        int width = board.getWidth();
        int[] dx = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] dy = {-1, 0, 1, -1, 1, -1, 0, 1};
        int maxNumber = -50;

        for (int i = 0; i < 8; i++) {
            int nearx = x + dx[i];
            int neary = y + dy[i];

            if (nearx >= 0 && nearx < width && neary >= 0 && neary < height && 0<current_board[neary][nearx] && 9>current_board[neary][nearx]) {
                if (current_board[neary][nearx] > maxNumber) {
                    maxNumber = current_board[neary][nearx]; 
                }
            }
        }
        if (0<=maxNumber && maxNumber<=4){
            return new int[] {1, -10};
        }
        else if (maxNumber==5){
            return new int[] {10,-20};
        }
        else if (maxNumber==6){
            return new int[] {20,-30};
        }
        else if (maxNumber==7){
            return new int[] {50,-40};
        }
        else{
            return new int[] {100,0};
        }
    }

    //x,yが不明マスと隣接した地雷でない開示マスであり、かつprobabilty_boardを仮定したときそのマスとの周囲の地雷条件を満たしているかどうか
    private boolean isConsistentWithRevealedNeighbors(int x, int y, int[][] probabilty_board,int[][] current_board) {
        int height = board.getHeight();
        int width = board.getWidth();
        int[] dx = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] dy = {-1, 0, 1, -1, 1, -1, 0, 1};
        int revealedCount = 0;
        if (0<current_board[y][x] &&current_board[y][x]<9 && hasUnrevealedNeighbor(x, y, current_board)){
            for (int i = 0; i < 8; i++) {
                int nearx = x + dx[i];
                int neary = y + dy[i];

                    if (nearx >= 0 && nearx < width && neary >= 0 && neary < height &&  (probabilty_board[neary][nearx] ==1 || current_board[neary][nearx]==10|| current_board[neary][nearx]==9)) {
                        revealedCount++;
                    }
                }

                return current_board[y][x] == revealedCount;
        }
        return false;
    }

    // x, yが非開示マスと隣接しているかつ、probabilty_boardで1もしくは0としてあてはめられた非開示マス(potentialBombsとindexから判断する)と開示マスですべてが囲まれて
    //おり、そのマスで矛盾があるかどうかを調べる関数
    //それが囲まれていないまたは、囲まれているが矛盾がない場合にfalseを返す
    private boolean isSurroundedByRevealedAndPotentialBombs(int x, int y,int index, int[][] probability_board, int[][] current_board, List<int[]> potentialBombs) {
        int height = board.getHeight();
        int width = board.getWidth();
        int[] dx = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] dy = {-1, 0, 1, -1, 1, -1, 0, 1};
        int numbombs=0;

        for (int i = 0; i < 8; i++) {
            int nearx = x + dx[i];
            int neary = y + dy[i];

            if (nearx >= 0 && nearx < width && neary >= 0 && neary < height) {
                // Check if the cell is unrevealed and not a potential bomb
                if ((current_board[neary][nearx] == -1 && isPotentialBomb(nearx, neary, index,potentialBombs))||(current_board[neary][nearx]!=-1)) {
                    if (current_board[neary][nearx]==-1){
                        numbombs+=probability_board[neary][nearx];
                    }
                    else if (current_board[neary][nearx]==9 || current_board[neary][nearx]==10){
                        numbombs+=1;
                    }
                } else{
                    return false;
                }
            }
        }
        return numbombs!=current_board[y][x];
    }


    //現在のindexの条件でprobabilty_boardに矛盾が存在していないのかどうかを調べる関数
    private boolean istrue_now(int index,int[][] probability_board, int[][] current_board, List<int[]> potentialBombs){
        int height = board.getHeight();
        int width = board.getWidth();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (0<current_board[y][x] && current_board[y][x]<9 && isSurroundedByRevealedAndPotentialBombs(x,y, index,probability_board,current_board, potentialBombs)) {
                    return false;     
                }
            }
        }
        return true;
    }

    // x,yがpotentialbombに含まれているかどうかを調べる関数
    private boolean isPotentialBomb(int x, int y,int index, List<int[]> potentialBombs) {
        for (int[] bomb : potentialBombs.subList(0, index+1)) {
            if (bomb[0] == x && bomb[1] == y) {
                return true;
            }
        }
        return false;
    }
    //再帰的に探索を行う関数
    private void generateProbabilityBoardsRecursive(int index, int[][] probability_board, List<int[]> potentialBombs, int[][] current_board, List<int[][]> allBoards) {
        if (index == potentialBombs.size()) {
            // それが矛盾がないかチェックする
            boolean isConsistent = true;
            for (int y = 0; y < board.getHeight() && isConsistent; y++) {
                for (int x = 0; x < board.getWidth() && isConsistent; x++) {
                    if (0 < current_board[y][x] && current_board[y][x] < 9 && hasUnrevealedNeighbor(x, y, current_board)) {
                        isConsistent = isConsistentWithRevealedNeighbors(x, y, probability_board, current_board);
                    }
                }
            }
            if (isConsistent) {
                //System.out.println(true);
                //System.out.println(Arrays.deepToString(probability_board));
                //矛盾がなければ候補に追加
                allBoards.add(copyBoard(probability_board));
            }
            return;
        }

        int[] coord = potentialBombs.get(index);
        int x = coord[0];
        int y = coord[1];

        // 爆弾なしでためす
        probability_board[y][x] = 0;
        //現時点で矛盾が発生しないか確認
        if (istrue_now(index,probability_board, current_board, potentialBombs)){
            generateProbabilityBoardsRecursive(index + 1, probability_board, potentialBombs, current_board, allBoards);
        }

        // 爆弾ありで試す
        probability_board[y][x] = 1;
        //現時点で矛盾が発生したらその時点で探索終了
        if (istrue_now(index,probability_board, current_board, potentialBombs)){
            generateProbabilityBoardsRecursive(index + 1, probability_board, potentialBombs, current_board, allBoards);
        }
    }

    // 再帰的に全ての可能なprobability_boardを生成する関数
    private List<int[][]> generateAllProbabilityBoards(int[][] current_board) {
        int height = board.getHeight();
        int width = board.getWidth();
        List<int[][]> allBoards = new ArrayList<>();

        // 1が入りうる座標をリストアップ
        List<int[]> potentialBombs = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (hasRevealedNeighbor(x, y, current_board) && current_board[y][x] == -1) {
                    potentialBombs.add(new int[]{x, y});
                }
            }
        }

        int[][] probability_board = new int[height][width];
        generateProbabilityBoardsRecursive(0, probability_board, potentialBombs, current_board, allBoards);

        return allBoards;
    }

    //リストをコピーする
    private int[][] copyBoard(int[][] board) {
        int[][] newBoard = new int[board.length][board[0].length];
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                newBoard[i][j] = board[i][j];
            }
        }
        return newBoard;
    }

    // 各セルの地雷である確率を計算する関数
    private double[][] calculateMineProbabilities( List<int[][]> allBoards, int[][] current_board, int height, int width) {
        double[][] mineProbabilities = new double[height][width];
        int totalBoards = allBoards.size();
        System.out.println(totalBoards);

        for (int[][] probability_board : allBoards) {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                        if (probability_board[y][x] == 1 || current_board[y][x]==9 ||current_board[y][x]==10) {
                            mineProbabilities[y][x] += 1.0;
                        }
                }
            }
        }

        // 各セルの確率を計算
        int a=0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                a+=mineProbabilities[y][x];
                mineProbabilities[y][x] /= totalBoards;          
            }
        }
        a/=totalBoards;
        a=board.getNumBombs()-a;
        int b=0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if(current_board[y][x]==-1 && !(hasRevealedNeighbor(x, y, current_board)&& current_board[y][x]==-1)){
                    b = b+1;
                }
            }
        }
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if(current_board[y][x]==-1 && !(hasRevealedNeighbor(x, y, current_board)&& current_board[y][x]==-1)){
                    mineProbabilities[y][x] = Math.round((double) a / b * 1000) / 1000.0;
                }
            }
        }
        //System.out.println(Arrays.deepToString(mineProbabilities));
        return mineProbabilities;
    }

    //与えられた確率の票からもっともスコアの期待値が高いものを選ぶ関数
    private void findAndActOnMostLikelyMine(int[][] current_board, double[][] mineProbabilities) {
        int height = mineProbabilities.length;
        int width = mineProbabilities[0].length;
        int[] targetCell = {-5, -5};
        double max_e= -100;
        boolean isFlag = false;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (current_board[y][x] == -1) {
                    double probability = mineProbabilities[y][x];
                    int basic_score = sumAdjacentNumbers(x, y, current_board);
                    int [] bairitu = maxAdjacentNumber(x, y, current_board);
                    if (basic_score!=0){
                        double frag_score_e = probability*basic_score*bairitu[0]+(1-probability)*basic_score*bairitu[1];
                        double open_cell_e = (1-probability)*basic_score*bairitu[0]+probability*basic_score*-10;
                        //相手に情報を与えないためフラッグ優先
                        if (frag_score_e>=open_cell_e){
                            if (max_e<frag_score_e){
                                max_e=frag_score_e;
                                isFlag=true;
                                targetCell[0]=x;
                                targetCell[1]=y;
                            }
                        }
                        else{
                            if (max_e<open_cell_e){
                                max_e=open_cell_e;
                                isFlag=false;
                                targetCell[0]=x;
                                targetCell[1]=y;
    
                            }
                        }
                    }
                    else{
                        double frag_score_e = probability*1+(1-probability)*-100;
                        double open_cell_e = (1-probability)*1+(probability)*-100;
                        if (frag_score_e>open_cell_e){
                            if (max_e<=frag_score_e){
                                max_e=frag_score_e;
                                isFlag=true;
                                targetCell[0]=x;
                                targetCell[1]=y;
                            }
                        }
                        else{
                            if (max_e<open_cell_e){
                                max_e=open_cell_e;
                                isFlag=false;
                                targetCell[0]=x;
                                targetCell[1]=y;
    
                            }
                        }
                    }                   
                }
            }
        }

        if (isFlag) {
            board.putFlag(targetCell[0], targetCell[1]);
        } else {
            board.openCell(targetCell[0], targetCell[1]);
        }
    }
    
    //フラッグ失敗かどうか判断する関数

    //自分が(x,y)マスを開けたときに1ターン後の相手が最適行動をとった場合の期待値を求めて、それとの比較から最適行動を割り出す関数

	// 行動する
    //すべての爆弾がおかれるパターンを再帰的に全探索し、条件を満たすもののみを取り出す。
    //取り出した結果から現在の状況からすべてのマスの爆弾がおかれている確率をもとめ、そこからもっともスコアの期待値の高い確率を求める。
	public void play() {
        int height=board.getHeight();
        int width=board.getWidth();
        int [][] current_board = new int[height][width];
        for (int y1 = 0; y1 < height; y1++) {
            for (int x1 = 0; x1 < width; x1++) {
                    current_board[y1][x1] = board.getCell(x1,y1);
                }
            }
        List<int[][]> allBoards=generateAllProbabilityBoards(current_board);
        double [][] mineProbabilities = calculateMineProbabilities(allBoards,current_board,height,width);
        findAndActOnMostLikelyMine(current_board, mineProbabilities);      
	}
}
