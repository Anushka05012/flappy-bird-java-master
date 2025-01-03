import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {
    int boardWidth = 360;
    int boardHeight = 640;

    // Images for Light and Dark Mode
    Image backgroundImg, birdImg, topPipeImg, bottomPipeImg, birdImgBlue;
    Image darkBackgroundImg, darkTopPipeImg, darkBottomPipeImg;

    // Bird class
    int birdX = boardWidth / 8;
    int birdY = boardWidth / 2;
    int birdWidth = 34;
    int birdHeight = 24;

    class Bird {
        int x = birdX;
        int y = birdY;
        int width = birdWidth;
        int height = birdHeight;
        Image img;

        Bird(Image img) {
            this.img = img;
        }
    }

    // Pipe class
    int pipeX = boardWidth;
    int pipeY = 0;
    int pipeWidth = 64;
    int pipeHeight = 512;

    class Pipe {
        int x = pipeX;
        int y = pipeY;
        int width = pipeWidth;
        int height = pipeHeight;
        Image img;
        boolean passed = false;

        Pipe(Image img) {
            this.img = img;
        }
    }

    // Game logic
    Bird bird;
    int velocityX = -4;
    int velocityY = 0;
    int gravity = 1;

    ArrayList<Pipe> pipes;
    Random random = new Random();

    Timer gameLoop;
    Timer placePipeTimer;
    boolean gameOver = false;
    double score = 0;
    double highestScore = 0;
    boolean isDarkMode = false; // Dark mode state
    boolean isBirdBlue = false;

    FlappyBird() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setFocusable(true);
        addKeyListener(this);

        // Load light mode images
        backgroundImg = new ImageIcon(getClass().getResource("./flappybirdbg.png")).getImage();
        birdImg = new ImageIcon(getClass().getResource("./redbird.png")).getImage();
        birdImgBlue = new ImageIcon(getClass().getResource("./bluebird.png")).getImage();
        topPipeImg = new ImageIcon(getClass().getResource("./toppipe.png")).getImage();
        bottomPipeImg = new ImageIcon(getClass().getResource("./bottompipe.png")).getImage();

        // Load dark mode images
        darkBackgroundImg = new ImageIcon(getClass().getResource("./background_dark.png")).getImage();
        darkTopPipeImg = new ImageIcon(getClass().getResource("./dark_toppipe.png")).getImage();
        darkBottomPipeImg = new ImageIcon(getClass().getResource("./dark_bottompipe.png")).getImage();

        // Initialize bird
        bird = new Bird(birdImg);
        // bird = new Bird(birdImgBlue);
        pipes = new ArrayList<>();

        // Place pipes timer
        placePipeTimer = new Timer(1500, e -> placePipes());
        placePipeTimer.start();

        // Game timer
        gameLoop = new Timer(1000 / 60, this);
        gameLoop.start();
    }

    void placePipes() {
        int randomPipeY = (int) (pipeY - pipeHeight / 4 - Math.random() * (pipeHeight / 2));
        int openingSpace = boardHeight / 4;

        Pipe topPipe = new Pipe(isDarkMode ? darkTopPipeImg : topPipeImg);
        topPipe.y = randomPipeY;
        pipes.add(topPipe);

        Pipe bottomPipe = new Pipe(isDarkMode ? darkBottomPipeImg : bottomPipeImg);
        bottomPipe.y = topPipe.y + pipeHeight + openingSpace;
        pipes.add(bottomPipe);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        // Background
        g.drawImage(isDarkMode ? darkBackgroundImg : backgroundImg, 0, 0, boardWidth, boardHeight, null);

        // Bird
        g.drawImage(birdImg, bird.x, bird.y, bird.width, bird.height, null);

        // Pipes
        for (Pipe pipe : pipes) {
            g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
        }

        // Score
        g.setColor(isDarkMode ? Color.lightGray : Color.white);
        g.setFont(new Font("Arial", Font.PLAIN, 32));
        g.drawString("Score: " + (int) score, 10, 35);

        // Highest Score
        g.setFont(new Font("Arial", Font.PLAIN, 24));
        g.drawString("High Score: " + (int) highestScore, 10, 65);

        // Game Over Message
        if (gameOver) {
            g.setColor(isDarkMode ? Color.red : Color.white);
            g.drawString("Game Over", boardWidth / 4, boardHeight / 2);
        }
    }

    public void move() {
        velocityY += gravity;
        bird.y += velocityY;
        bird.y = Math.max(bird.y, 0);

        for (Pipe pipe : pipes) {
            pipe.x += velocityX;

            if (!pipe.passed && bird.x > pipe.x + pipe.width) {
                score += 0.5;
                pipe.passed = true;
            }

            if (collision(bird, pipe)) {
                gameOver = true;
            }
        }

        if (bird.y > boardHeight) {
            gameOver = true;
        }
    }

    boolean collision(Bird a, Pipe b) {
        return a.x < b.x + b.width &&
               a.x + a.width > b.x &&
               a.y < b.y + b.height &&
               a.y + a.height > b.y;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver) {
            placePipeTimer.stop();
            gameLoop.stop();
            highestScore = Math.max(highestScore, score);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            velocityY = -9;
    
            if (gameOver) {
                bird.y = birdY;
                velocityY = 0;
                pipes.clear();
                gameOver = false;
                score = 0;
                gameLoop.start();
                placePipeTimer.start();
            }
        } else if (e.getKeyCode() == KeyEvent.VK_D) { // Toggle dark mode
            isDarkMode = !isDarkMode;
    
            // Update the images of existing pipes to match the current mode
            for (Pipe pipe : pipes) {
                if (pipe.y < 0) { // Top pipe
                    pipe.img = isDarkMode ? darkTopPipeImg : topPipeImg;
                } else { // Bottom pipe
                    pipe.img = isDarkMode ? darkBottomPipeImg : bottomPipeImg;
                }
            }
        } else if (e.getKeyCode() == KeyEvent.VK_UP) { // Increase bird size
            if(bird.width < 50 && bird.height < 50){
            bird.width += 10;
            bird.height += 10;
            velocityX += 2;
            }
        }else if (e.getKeyCode() == KeyEvent.VK_DOWN) { 
            if(bird.width > 20 && bird.height > 20){// Increase bird size
            bird.width -= 5;
            bird.height -= 5;
            velocityX -=2;
        }
        }else if(e.getKeyCode() == KeyEvent.VK_C){
            isBirdBlue = !isBirdBlue;

            birdImg  = birdImgBlue;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}
}
