package SOS449;

import javax.swing.Timer;

public class SOSGameController {
    private SOSGame model;
    private SOSGameView view;

    public SOSGameController() {
    }

    public void setView(SOSGameView view) {
        this.view = view;
    }

    private ComputerOpponent computerOpponent; 
    private ComputerOpponent computerOpponent1; 
    private ComputerOpponent computerOpponent2; 

    public void startGame(boolean isVsComputer) {
        try {
            String boardSizeStr = view.getBoardSizeInput();
            int boardSize = Integer.parseInt(boardSizeStr);

            if (boardSize < 3) {
                throw new NumberFormatException("Board size must be at least 3.");
            }

            boolean isSimpleGame = view.isSimpleGameSelected();

            if (isSimpleGame) {
                model = new SimpleSOSGame(boardSize);
            } else {
                model = new GeneralSOSGame(boardSize);
            }

            if (isVsComputer) {
                computerOpponent = new ComputerOpponent(model, 'S'); 
            }

            view.showGameScreen(boardSize, model);
            view.updateTurnLabel("Player 1's turn");

        } catch (NumberFormatException ex) {
            view.showErrorMessage("Invalid board size. Please enter a number greater than or equal to 3.");
        }
    }

    public void startComputerVsComputerGame(boolean isSimpleGame) {
        try {
            String boardSizeStr = view.getBoardSizeInput();
            int boardSize = Integer.parseInt(boardSizeStr);

            if (boardSize < 3) {
                throw new NumberFormatException("Board size must be at least 3.");
            }

            if (isSimpleGame) {
                model = new SimpleSOSGame(boardSize);
            } else {
                model = new GeneralSOSGame(boardSize);
            }

            computerOpponent1 = new ComputerOpponent(model, 'S');
            computerOpponent2 = new ComputerOpponent(model, 'O');

            view.showGameScreen(boardSize, model);

            runComputerVsComputerGame();

        } catch (NumberFormatException ex) {
            view.showErrorMessage("Invalid board size. Please enter a number greater than or equal to 3.");
        }
    }

    private void runComputerVsComputerGame() {
        Timer timer = new Timer(1000, e -> {
            if (!model.isGameOver()) {
                ComputerOpponent currentComputer = model.isPlayerOneTurn() ? computerOpponent1 : computerOpponent2;
                int[] move = currentComputer.makeMove();
                if (move[0] >= 0 && move[1] >= 0) {
                    view.updateBoard(model.isPlayerOneTurn(), move[0], move[1], currentComputer.getComputerLetter());
                    view.updateTurnLabel("Player " + (model.isPlayerOneTurn() ? "1" : "2") + "'s turn");
                }
            } else {
                ((Timer) e.getSource()).stop(); 
                handleGameOver();
            }
        });

        timer.setInitialDelay(0);
        timer.start();
    }
    
    public void handleCellClick(int row, int col, char letter) {
        if (model.isGameOver()) {
            view.showErrorMessage("The game is already over. No more moves allowed.");
            return;
        }

        boolean isPlayerOneTurn = model.isPlayerOneTurn(); 

        System.out.println("handleCellClick called: Player " + (isPlayerOneTurn ? "1" : "2") + " is placing " + letter);

        if (model.placeLetter(row, col, letter)) {
            System.out.println("Letter " + letter + " placed successfully at (" + row + ", " + col + ")");
            view.updateBoard(isPlayerOneTurn, row, col, letter);

            view.updateTurnLabel("Player " + (model.isPlayerOneTurn() ? "1" : "2") + "'s turn");
            System.out.println("After placeLetter: It is now Player " + (model.isPlayerOneTurn() ? "1" : "2") + "'s turn");

            if (computerOpponent != null && !model.isPlayerOneTurn() && !model.isGameOver()) {
                int[] move = computerOpponent.makeMove();
                if (move[0] >= 0 && move[1] >= 0) {
                    int compRow = move[0];
                    int compCol = move[1];

                    char computerMoveLetter = computerOpponent.getComputerLetter();
                    view.updateBoard(false, compRow, compCol, computerMoveLetter);
                    System.out.println("Computer placed " + computerMoveLetter + " at (" + compRow + ", " + compCol + ")");

                    view.updateTurnLabel("Player " + (model.isPlayerOneTurn() ? "1" : "2") + "'s turn");
                    System.out.println("After computer move: It is now Player " + (model.isPlayerOneTurn() ? "1" : "2") + "'s turn");
                }
            }

            if (model.isGameOver()) {
                handleGameOver();
            }
        } else {
            view.showErrorMessage("Invalid move. The cell is already occupied.");
        }
    }

    private void handleGameOver() {
        String message;
        if (model instanceof GeneralSOSGame) {
            GeneralSOSGame generalGame = (GeneralSOSGame) model;
            int player1Score = generalGame.getPlayer1Score();
            int player2Score = generalGame.getPlayer2Score();
            if (player1Score > player2Score) {
                message = "Game over! Player 1 wins!";
            } else if (player2Score > player1Score) {
                message = "Game over! Player 2 wins!";
            } else {
                message = "Game over! It's a tie!";
            }
        } else if (model instanceof SimpleSOSGame) {
            SimpleSOSGame simpleGame = (SimpleSOSGame) model;
            if (simpleGame.isGameWon()) {
                message = simpleGame.getWinner() + " wins!";
            } else {
                message = "Game over! It's a draw!";
            }
        } else {
            message = "Game over! It's a draw!";
        }
        view.showWinner(message);
        view.resetToInitialSetup();
    }
}
