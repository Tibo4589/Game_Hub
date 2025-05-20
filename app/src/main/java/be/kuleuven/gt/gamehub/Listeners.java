package be.kuleuven.gt.gamehub;

public interface Listeners {
    public interface GameOverListener {
        void onGameOver(int score);
    }

    public interface OnScoreChangeListener {
        void onScoreChanged(int newScore);
    }
    public interface OnGridChangedListener {
        void onGridChanged();
    }
}
