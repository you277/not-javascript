import java.util.ArrayList;
import java.util.Scanner;
public class Game {
    private Player player = null;
    private ArrayList<Enemy> enemies;
    private ArrayList<Projectile> projectiles;
    private Grid grid = null;
    private boolean alive;
    private int currentStep;
    private int score;
    private boolean scoreExceededIntMax;
    private int enemySpawnPeriod; // how many steps it takes for another enemy to spawn
    private int enemyMovePeriod; // how many steps it takes for enemies to move
    private final Scanner s;

    void render() {
        ArrayList<Tile> tiles = new ArrayList<>();
        for (Enemy enemy: enemies) {
            tiles.add(enemy.getTile());
        }
        for (Projectile projectile: projectiles) {
            tiles.add(projectile.getTile());
        }
        if (alive) {
            tiles.add(player.getTile());
        }
        System.out.println(grid.getGridArt(tiles));
        if (scoreExceededIntMax) {
            System.out.println("score: bruh");
        } else {
            System.out.println("score: " + score);
        }
        System.out.println("enemies: " + enemies.size());
    }

    void stepEnemies() {
        if (currentStep % enemyMovePeriod != 0) {
            return;
        }
        Coordinate playerCoordinates = player.getTile().getCoordinates();
        int x = playerCoordinates.getX();
        int y = playerCoordinates.getY();
        for (Enemy enemy: enemies) {
            enemy.step(enemies, x, y);
        }
    }
    void stepProjectiles() {
        ArrayList<Projectile> projectilesToRemove = new ArrayList<>();
        for (Projectile projectile: projectiles) {
            if (projectile.getLife() == 0) {
                projectilesToRemove.add(projectile);
                continue;
            }
            projectile.step();
        }
        if (projectilesToRemove.size() != 0) {
            for (Projectile projectile: projectilesToRemove) {
                projectiles.remove(projectile);
            }
        }
    }

    void processProjectileCollisions() {
        if (projectiles.size() == 0 || enemies.size() == 0) {
            return;
        }
        ArrayList<Enemy> enemiesToRemove = new ArrayList<>();
        ArrayList<Projectile> projectilesToRemove = new ArrayList<>();
        for (Projectile projectile: projectiles) {
            Coordinate projectileCoordinates = projectile.getTile().getCoordinates();
            int x = projectileCoordinates.getX();
            int y = projectileCoordinates.getY();
            for (Enemy enemy: enemies) {
                Coordinate enemyCoordinates = enemy.getTile().getCoordinates();
                if (enemyCoordinates.getX() == x && enemyCoordinates.getY() == y) {
                    enemiesToRemove.add(enemy);
                    projectilesToRemove.add(projectile);
                    score += 2;
                }
            }
        }
        if (enemiesToRemove.size() == 0) {
            return;
        }
        for (Projectile projectile: projectilesToRemove) {
            projectiles.remove(projectile);
        }
        for (Enemy enemy: enemiesToRemove) {
            enemies.remove(enemy);
        }
    }

    void processPlayerCollisions() {
        Coordinate playerCoordinates = player.getTile().getCoordinates();
        int x = playerCoordinates.getX();
        int y = playerCoordinates.getY();
        for (Enemy enemy: enemies) {
            Coordinate enemyCoordinates = enemy.getTile().getCoordinates();
            if (enemyCoordinates.getX() == x && enemyCoordinates.getY() == y) {
                alive = false;
                break;
            }
        }
    }

    void spawnEnemies() {
        if (currentStep % enemySpawnPeriod != 0) {
            return;
        }

        Coordinate playerCoordinates = player.getTile().getCoordinates();
        int x = playerCoordinates.getX();
        int y = playerCoordinates.getY();

        int xOffset = 3 +(int)Math.round(Math.random()*7);
        int yOffset = 3 + (int)Math.round(Math.random()*7);
        if (Math.random() > 0.5) {
            xOffset *= -1;
        }
        if (Math.random() > 0.5) {
            yOffset *= -1;
        }

        Enemy enemy = new Enemy(
                x + xOffset,
                y + yOffset
        );
        enemies.add(enemy);
    }

    void step() {
        stepProjectiles();
        processProjectileCollisions();
        player.step();
        stepEnemies();
        processPlayerCollisions();
        spawnEnemies();
        if (score == Integer.MAX_VALUE) { // should never happen but whatever
            scoreExceededIntMax = true;
        }
        currentStep++;
        score++;
    }

    void pointArrow() {
        String direction = player.getDirection();
        Tile playerTile = player.getTile();

        switch (direction) {
            case "up" -> playerTile.setCharacter("⏫");
            case "left" -> playerTile.setCharacter("⏪");
            case "down" -> playerTile.setCharacter("⏬");
            case "right" -> playerTile.setCharacter("⏩");
        }
    }

    void processInput(String input) {
        switch (input) {
            case "w" -> player.setDirection("up");
            case "a" -> player.setDirection("left");
            case "s" -> player.setDirection("down");
            case "d" -> player.setDirection("right");
            case "1" -> {
                Coordinate playerCoordinate = player.getTile().getCoordinates();
                Projectile projectile = new Projectile(playerCoordinate.getX(), playerCoordinate.getY(), player.getDirection());
                projectile.setBounds(-10, 10, -10, 10);
                projectiles.add(projectile);
            }
        }
    }

    boolean doIntro() {
        System.out.println(
                """

                           ________  ________  ________      ________  ________  ________      ________  ________  ________  ________  \s
                          /        \\/        \\/  /  /  \\    /        \\/        \\/  /  /  \\    /        \\/        \\/        \\/        \\ \s
                         /         /         /         /   /         /         /         /   /       __/         /         /         / \s
                        /       __/        _/         /   /       __/        _/         /   /       / /         /         /        _/  \s
                        \\______/  \\________/\\________/    \\______/  \\________/\\________/    \\________/\\___/____/\\__/__/__/\\________/   \s

                                                                                                         _\s
                            ____  ___ _      __   ____  ___ _      __   ____ _____ _____ ___  ___     _ | |
                           / __ \\/ _ \\ | /| / /  / __ \\/ _ \\ | /| / /  / __ `/ __ `/ __ `__ \\/ _ \\   (_)/ /
                          / /_/ /  __/ |/ |/ /  / /_/ /  __/ |/ |/ /  / /_/ / /_/ / / / / / /  __/  _  / /\s
                         / .___/\\___/|__/|__/  / .___/\\___/|__/|__/   \\__, /\\__,_/_/ /_/ /_/\\___/  (_)/_/ \s
                        /_/                   /_/                    /____/                         /_/   \s

                                                                                        """
        );

        String input;
        while (true) {
            System.out.println("\nbegin (y/n)");
            input = s.nextLine();
            if (input.equals("y") || input.equals("n")) {
                break;
            } else {
                if (input.equals("y/n")) {
                    System.out.println("oh you think ur so funny");
                } else {
                    System.out.println("you FOOL provide CORRECT input or you will...");
                }
            }
        }
        return input.equals("y");
    }

    void onLose() {
        System.out.println("womp womp");
        System.out.println("final score: " + score);
    }

    public void doGame() {
        if (alive) { return; }
        alive = true;

        player = new Player();
        grid = new Grid();
        enemies = new ArrayList<>();
        projectiles = new ArrayList<>();
        currentStep = 0;
        enemySpawnPeriod = 3;
        enemyMovePeriod = 2;

        Grid.setRespectCenter(false);
        player.setBounds(-10, 10, -10, 10);

        while (alive) {
            render();
            System.out.println("change direction (wasd) or shoot (1): ");
            processInput(s.nextLine());
            step();
            pointArrow();
        }
        render();
        onLose();
    }

    boolean getRestartInput() {
        while (true) {
            System.out.println("\nplay again? (y/n)");
            String input = s.nextLine();
            if (input.equals("y")) {
                return true;
            } else if (input.equals("n")){
                return false;
            } else {
                System.out.println("you fool.");
            }
        }
    }

    public void start() {
        boolean startGame = doIntro();
        if (!startGame) {
            alive = false;
            System.out.println("ok bye");
            return;
        }
        doGame();
        while (true) {
            if (getRestartInput()) {
                doGame();
            } else {
                break;
            }
        }
    }

    public Game() {
        s = new Scanner(System.in);
    }
}
