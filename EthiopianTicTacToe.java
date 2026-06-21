import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.*;

public class EthiopianTicTacToe extends JFrame {

   
    static final Color FLAG_GREEN  = new Color(7, 137, 48);
    static final Color FLAG_YELLOW = new Color(252, 221, 9);
    static final Color FLAG_RED    = new Color(218, 18, 26);
    static final Color BG_DARK     = new Color(18, 18, 20);
    static final Color PANEL_DARK  = new Color(30, 30, 33);
    static final Color SPEAR_WOOD  = new Color(133, 87, 35);
    static final Color SPEAR_BIND  = FLAG_YELLOW;
    static final Color SPEAR_HEAD  = new Color(214, 214, 220);
    static final Color SHIELD_RIM  = new Color(60, 40, 20);

    private final char[] board = new char[9];
    private boolean humanTurn = true;    
    private boolean gameOver = false;
    private int[] winningLine = null;

    private String difficulty = "Medium";
    private int timeLeft = 15;
    private final int TURN_SECONDS = 15;
    private Timer countdownTimer;
    private Timer aiThinkTimer;

    private int humanWins = 0, aiWins = 0, draws = 0;

    private final BoardPanel boardPanel = new BoardPanel();
    private JLabel statusLabel;
    private JLabel timerLabel;
    private JLabel scoreLabel;
    private JComboBox<String> levelBox;

    public EthiopianTicTacToe() {
        super("Ethiopian Tic Tac Toe");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout());

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(BG_DARK);
        center.add(buildHeader(), BorderLayout.NORTH);
        center.add(boardPanel, BorderLayout.CENTER);
        center.add(buildFooter(), BorderLayout.SOUTH);

        add(buildFlagStripe(), BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);

        boardPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleCellClick(e.getX(), e.getY());
            }
        });

        initBoard();
        pack();
        setLocationRelativeTo(null);
    }

    private JPanel buildFlagStripe() {
        JPanel p = new JPanel(new GridLayout(3, 1));
        p.setPreferredSize(new Dimension(10, 18));
        JPanel g = new JPanel(); g.setBackground(FLAG_GREEN);
        JPanel y = new JPanel(); y.setBackground(FLAG_YELLOW);
        JPanel r = new JPanel(); r.setBackground(FLAG_RED);
        p.add(g); p.add(y); p.add(r);
        return p;
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel();
        header.setBackground(BG_DARK);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(BorderFactory.createEmptyBorder(14, 10, 10, 10));

        JLabel title = new JLabel("ETHIOPIAN TIC TAC TOE");
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        title.setForeground(FLAG_YELLOW);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Spears \u2694 vs Shield \u2B55  \u2022  \u12A5\u1295\u12B3\u1295 \u12F0\u1206\u1293 \u1218\u1321");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(new Color(200, 200, 200));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        controls.setBackground(BG_DARK);

        JLabel levelLbl = new JLabel("AI Level:");
        levelLbl.setForeground(Color.WHITE);
        levelBox = new JComboBox<>(new String[]{"Easy", "Medium", "Hard"});
        levelBox.setSelectedItem(difficulty);
        levelBox.addActionListener(e -> {
            difficulty = (String) levelBox.getSelectedItem();
            startNewGame();
        });

        JButton newGameBtn = new JButton("New Game");
        styleButton(newGameBtn, FLAG_GREEN);
        newGameBtn.addActionListener(e -> startNewGame());

        JButton resetScoreBtn = new JButton("Reset Score");
        styleButton(resetScoreBtn, FLAG_RED);
        resetScoreBtn.addActionListener(e -> {
            humanWins = 0; aiWins = 0; draws = 0;
            updateScoreLabel();
        });

        controls.add(levelLbl);
        controls.add(levelBox);
        controls.add(newGameBtn);
        controls.add(resetScoreBtn);

        scoreLabel = new JLabel();
        scoreLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        scoreLabel.setForeground(new Color(220, 220, 220));
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        updateScoreLabel();

        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitle);
        header.add(Box.createVerticalStrut(6));
        header.add(controls);
        header.add(scoreLabel);
        return header;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel();
        footer.setBackground(BG_DARK);
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setBorder(BorderFactory.createEmptyBorder(10, 10, 16, 10));

        statusLabel = new JLabel("Your move, warrior. Place a spear (X).");
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        timerLabel = new JLabel("Time left: 15s");
        timerLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        timerLabel.setForeground(FLAG_YELLOW);
        timerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        footer.add(statusLabel);
        footer.add(Box.createVerticalStrut(4));
        footer.add(timerLabel);
        return footer;
    }

    private void styleButton(JButton b, Color c) {
        b.setBackground(c);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
    }

    private void updateScoreLabel() {
        scoreLabel.setText("You: " + humanWins + "    AI: " + aiWins + "    Draws: " + draws);
    }


    private void initBoard() {
        for (int i = 0; i < 9; i++) board[i] = ' ';
        humanTurn = true;
        gameOver = false;
        winningLine = null;
        timeLeft = TURN_SECONDS;
        statusLabel.setText("Your move, warrior. Place a spear (X).");
        startCountdown();
        boardPanel.repaint();
    }

    private void startNewGame() {
        stopAllTimers();
        initBoard();
    }

    private void stopAllTimers() {
        if (countdownTimer != null) countdownTimer.stop();
        if (aiThinkTimer != null) aiThinkTimer.stop();
    }

    private void startCountdown() {
        if (countdownTimer != null) countdownTimer.stop();
        timeLeft = TURN_SECONDS;
        timerLabel.setText("Time left: " + timeLeft + "s");
        timerLabel.setForeground(FLAG_YELLOW);
        countdownTimer = new Timer(1000, e -> {
            if (gameOver || !humanTurn) {
                countdownTimer.stop();
                return;
            }
            timeLeft--;
            timerLabel.setText("Time left: " + timeLeft + "s");
            if (timeLeft <= 5) timerLabel.setForeground(FLAG_RED);
            if (timeLeft <= 0) {
                countdownTimer.stop();
                forceRandomHumanMove();
            }
        });
        countdownTimer.start();
    }

    private void forceRandomHumanMove() {
        List<Integer> empties = emptyCells(board);
        if (empties.isEmpty() || gameOver) return;
        int idx = empties.get(new Random().nextInt(empties.size()));
        statusLabel.setText("Time's up! A spear was thrown at random.");
        playMove(idx, 'X');
    }

    private void handleCellClick(int x, int y) {
        if (gameOver || !humanTurn) return;
        int cellSize = boardPanel.getWidth() / 3;
        if (cellSize <= 0) return;
        int col = x / cellSize;
        int row = y / cellSize;
        if (col < 0 || col > 2 || row < 0 || row > 2) return;
        int idx = row * 3 + col;
        if (board[idx] != ' ') return;
        playMove(idx, 'X');
    }

    private void playMove(int idx, char mark) {
        if (idx < 0 || idx > 8 || board[idx] != ' ' || gameOver) return;
        board[idx] = mark;
        boardPanel.repaint();

        char winner = checkWinner(board);
        if (winner != 0) {
            finishGame(winner);
            return;
        }

        humanTurn = (mark != 'X');
        if (humanTurn) {
            statusLabel.setText("Your move, warrior. Place a spear (X).");
            startCountdown();
        } else {
            if (countdownTimer != null) countdownTimer.stop();
            statusLabel.setText("The defender is thinking...");
            triggerAiMove();
        }
    }

    private void triggerAiMove() {
        if (aiThinkTimer != null) aiThinkTimer.stop();
        int delay = "Easy".equals(difficulty) ? 350 : "Medium".equals(difficulty) ? 550 : 750;
        aiThinkTimer = new Timer(delay, e -> {
            aiThinkTimer.stop();
            int move = chooseAiMove();
            playMove(move, 'O');
        });
        aiThinkTimer.setRepeats(false);
        aiThinkTimer.start();
    }

    private void finishGame(char winner) {
        gameOver = true;
        if (countdownTimer != null) countdownTimer.stop();
        if (aiThinkTimer != null) aiThinkTimer.stop();
        winningLine = getWinningLine(board);

        if (winner == 'X') {
            humanWins++;
            statusLabel.setText("Victory! Your spears pierced the shield wall.");
        } else if (winner == 'O') {
            aiWins++;
            statusLabel.setText("Defeat! The shield held strong.");
        } else {
            draws++;
            statusLabel.setText("A noble stalemate. The battlefield is even.");
        }
        timerLabel.setText("Game over");
        timerLabel.setForeground(new Color(180, 180, 180));
        updateScoreLabel();
        boardPanel.repaint();
    }

    private static final int[][] LINES = {
        {0,1,2}, {3,4,5}, {6,7,8},
        {0,3,6}, {1,4,7}, {2,5,8},
        {0,4,8}, {2,4,6}
    };

    private char checkWinner(char[] b) {
        for (int[] line : LINES) {
            char a = b[line[0]], c = b[line[1]], d = b[line[2]];
            if (a != ' ' && a == c && c == d) return a;
        }
        for (char v : b) if (v == ' ') return 0;
        return 'D'; // draw
    }

    private int[] getWinningLine(char[] b) {
        for (int[] line : LINES) {
            char a = b[line[0]], c = b[line[1]], d = b[line[2]];
            if (a != ' ' && a == c && c == d) return line;
        }
        return null;
    }

    private List<Integer> emptyCells(char[] b) {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 9; i++) if (b[i] == ' ') list.add(i);
        return list;
    }

    private int chooseAiMove() {
        List<Integer> empties = emptyCells(board);
        Random rnd = new Random();

        if ("Easy".equals(difficulty)) {
            return empties.get(rnd.nextInt(empties.size()));
        }

        if ("Medium".equals(difficulty)) {
            
            Integer winNow = findImmediateMove(board, 'O');
            if (winNow != null) return winNow;
            Integer blockNow = findImmediateMove(board, 'X');
            if (blockNow != null) return blockNow;
            if (rnd.nextDouble() < 0.5) {
                return bestMoveIndex(board);
            }
            return empties.get(rnd.nextInt(empties.size()));
        }

        
        return bestMoveIndex(board);
    }

    private Integer findImmediateMove(char[] b, char mark) {
        for (int i = 0; i < 9; i++) {
            if (b[i] == ' ') {
                b[i] = mark;
                boolean wins = checkWinner(b) == mark;
                b[i] = ' ';
                if (wins) return i;
            }
        }
        return null;
    }

    private int bestMoveIndex(char[] b) {
        int bestScore = Integer.MIN_VALUE;
        int bestIdx = -1;
        for (int i = 0; i < 9; i++) {
            if (b[i] == ' ') {
                b[i] = 'O';
                int score = minimax(b, false, 0);
                b[i] = ' ';
                if (score > bestScore) {
                    bestScore = score;
                    bestIdx = i;
                }
            }
        }
        return bestIdx;
    }

    private int minimax(char[] b, boolean maximizing, int depth) {
        char winner = checkWinner(b);
        if (winner == 'O') return 10 - depth;
        if (winner == 'X') return depth - 10;
        if (winner == 'D') return 0;

        if (maximizing) {
            int best = Integer.MIN_VALUE;
            for (int i = 0; i < 9; i++) {
                if (b[i] == ' ') {
                    b[i] = 'O';
                    best = Math.max(best, minimax(b, false, depth + 1));
                    b[i] = ' ';
                }
            }
            return best;
        } else {
            int best = Integer.MAX_VALUE;
            for (int i = 0; i < 9; i++) {
                if (b[i] == ' ') {
                    b[i] = 'X';
                    best = Math.min(best, minimax(b, true, depth + 1));
                    b[i] = ' ';
                }
            }
            return best;
        }
    }

    class BoardPanel extends JPanel {
        BoardPanel() {
            setPreferredSize(new Dimension(450, 450));
            setBackground(PANEL_DARK);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            int w = getWidth(), h = getHeight();
            int cell = w / 3;

            
            g2.setColor(PANEL_DARK);
            g2.fillRect(0, 0, w, h);

            
            g2.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(FLAG_YELLOW);
            g2.drawLine(cell, 10, cell, h - 10);
            g2.drawLine(cell * 2, 10, cell * 2, h - 10);
            g2.drawLine(10, cell, w - 10, cell);
            g2.drawLine(10, cell * 2, w - 10, cell * 2);

            
            for (int i = 0; i < 9; i++) {
                int row = i / 3, col = i % 3;
                int cx = col * cell + cell / 2;
                int cy = row * cell + cell / 2;
                int size = (int) (cell * 0.66);
                if (board[i] == 'X') {
                    drawSpearsX(g2, cx, cy, size);
                } else if (board[i] == 'O') {
                    drawShieldO(g2, cx, cy, size);
                }
            }


            if (winningLine != null) {
                int a = winningLine[0], c = winningLine[2];
                int ax = (a % 3) * cell + cell / 2, ay = (a / 3) * cell + cell / 2;
                int cx2 = (c % 3) * cell + cell / 2, cy2 = (c / 3) * cell + cell / 2;
                g2.setStroke(new BasicStroke(10, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.setColor(new Color(FLAG_GREEN.getRed(), FLAG_GREEN.getGreen(), FLAG_GREEN.getBlue(), 170));
                g2.drawLine(ax, ay, cx2, cy2);
            }
        }

        
        private void drawSpearsX(Graphics2D g2, int cx, int cy, int size) {
            int half = size / 2;
            drawSingleSpear(g2, cx - half, cy - half, cx + half, cy + half);
            drawSingleSpear(g2, cx + half, cy - half, cx - half, cy + half);
        }

        private void drawSingleSpear(Graphics2D g2, int x1, int y1, int x2, int y2) {
            double angle = Math.atan2(y2 - y1, x2 - x1);

            
            g2.setStroke(new BasicStroke(7, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(SPEAR_WOOD);
            g2.drawLine(x1, y1, x2, y2);


            drawSpearhead(g2, x1, y1, angle + Math.PI);
            drawSpearhead(g2, x2, y2, angle);
        }

        private void drawSpearhead(Graphics2D g2, int tipX, int tipY, double angle) {
            int headLen = 16;
            int headWidth = 7;
            double bx = tipX - Math.cos(angle) * headLen;
            double by = tipY - Math.sin(angle) * headLen;
            double perpX = -Math.sin(angle) * headWidth;
            double perpY = Math.cos(angle) * headWidth;

            Path2D head = new Path2D.Double();
            head.moveTo(tipX, tipY);
            head.lineTo(bx + perpX, by + perpY);
            head.lineTo(bx - perpX, by - perpY);
            head.closePath();
            g2.setColor(SPEAR_HEAD);
            g2.fill(head);
            g2.setColor(new Color(90, 90, 95));
            g2.setStroke(new BasicStroke(1.2f));
            g2.draw(head);

            
            double wrapX = bx - Math.cos(angle) * 3;
            double wrapY = by - Math.sin(angle) * 3;
            g2.setColor(SPEAR_BIND);
            g2.setStroke(new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
            g2.draw(new Line2D.Double(
                wrapX + perpX * 0.9, wrapY + perpY * 0.9,
                wrapX - perpX * 0.9, wrapY - perpY * 0.9));
        }

        
        private void drawShieldO(Graphics2D g2, int cx, int cy, int size) {
            int r = size / 2;
            Ellipse2D outline = new Ellipse2D.Double(cx - r, cy - r, size, size);

            
            Shape oldClip = g2.getClip();
            g2.clip(outline);
            g2.setColor(FLAG_GREEN);
            g2.fillRect(cx - r, cy - r, size, size / 3 + 2);
            g2.setColor(FLAG_YELLOW);
            g2.fillRect(cx - r, cy - r + size / 3, size, size / 3 + 2);
            g2.setColor(FLAG_RED);
            g2.fillRect(cx - r, cy + r - size / 3 - 1, size, size / 3 + 2);
            g2.setClip(oldClip);


            g2.setStroke(new BasicStroke(4f));
            g2.setColor(SHIELD_RIM);
            g2.draw(outline);

            
            g2.setColor(new Color(225, 225, 230));
            int studCount = 8;
            int studR = 3;
            for (int i = 0; i < studCount; i++) {
                double a = 2 * Math.PI * i / studCount;
                int sx = (int) (cx + Math.cos(a) * (r - 6));
                int sy = (int) (cy + Math.sin(a) * (r - 6));
                g2.fillOval(sx - studR, sy - studR, studR * 2, studR * 2);
            }

            
            int bossR = Math.max(6, r / 4);
            RadialGradientPaint boss = new RadialGradientPaint(
                new Point(cx, cy), bossR,
                new float[]{0f, 1f},
                new Color[]{new Color(235, 235, 240), new Color(120, 120, 125)});
            g2.setPaint(boss);
            g2.fillOval(cx - bossR, cy - bossR, bossR * 2, bossR * 2);
            g2.setColor(SHIELD_RIM);
            g2.setStroke(new BasicStroke(2f));
            g2.drawOval(cx - bossR, cy - bossR, bossR * 2, bossR * 2);
        }
    }

    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new EthiopianTicTacToe().setVisible(true));
    }
}
