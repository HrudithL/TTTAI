# TTTAI - 4x4x4 Tic Tac Toe Game

A Java-based implementation of a three-dimensional Tic Tac Toe game with a 4x4x4 board structure. This project provides a foundation for building AI players and a graphical user interface for the game.

## Project Overview

This project implements a 4x4x4 Tic Tac Toe game using Java Swing for the graphical interface. The game extends the traditional 2D Tic Tac Toe concept into three dimensions, creating a more complex and challenging gameplay experience.

## Features

- **3D Game Board**: 4x4x4 three-dimensional game board
- **Modular Player System**: Interface-based player system allowing for easy AI implementation
- **Graphical Interface**: Java Swing-based GUI for game visualization
- **Extensible Architecture**: Clean separation of concerns for easy extension

## Project Structure

```
TTTAI/
├── src/
│   ├── Main.java          # Entry point of the application
│   ├── TFrame.java        # Main application window (JFrame)
│   ├── TPanel.java        # Game panel for rendering (JPanel)
│   ├── Player.java        # Base player implementation
│   ├── PlayerInt.java     # Player interface definition
│   └── Location.java      # 3D coordinate representation
└── TTTAI.iml             # IntelliJ IDEA module configuration
```

## Classes Description

### Main.java
The entry point of the application. Creates and displays the main game window.

### TFrame.java
Extends `JFrame` to create the main application window. Handles window setup, sizing, and contains the game panel.

**Key Methods:**
- `TFrame(String frameName)` - Constructor that sets up the window
- `getP()` - Returns the game panel

### TPanel.java
Extends `JPanel` to handle the game rendering. Currently contains a placeholder method `oneBoard()` for drawing individual game boards.

**Key Methods:**
- `oneBoard(Graphics g, int x, int y)` - Placeholder for rendering individual boards

### PlayerInt.java
Interface defining the contract for all players in the game.

**Required Methods:**
- `getLetter()` - Returns the player's letter (X or O)
- `getMove(char[][][] board)` - Returns the player's next move
- `getName()` - Returns the player's name
- `reset()` - Resets player state for a new game

### Player.java
Base implementation of the `PlayerInt` interface. Currently contains placeholder implementations that return null/empty values.

### Location.java
Represents a 3D coordinate position on the game board.

**Properties:**
- `col` - Column (x-coordinate)
- `row` - Row (y-coordinate)  
- `sheet` - Sheet/level (z-coordinate)

**Key Methods:**
- `Location(int col, int row, int sheet)` - Constructor
- `getCol()`, `getRow()`, `getSheet()` - Getters for coordinates
- `toString()` - String representation of the location

## Game Rules

The game follows traditional Tic Tac Toe rules but in three dimensions:
- Players alternate turns placing their marks (X or O)
- The goal is to get 4 marks in a row (horizontally, vertically, or diagonally)
- Winning lines can span across the 3D board in any direction
- The game ends when a player wins or the board is full

## Getting Started

### Prerequisites
- Java Development Kit (JDK) 8 or higher
- IntelliJ IDEA (recommended) or any Java IDE

### Running the Application
1. Clone or download the project
2. Open the project in your IDE
3. Compile and run `Main.java`
4. The game window will appear with the title "4x4x4 Tic Tac Toe"

## Development Status

This project appears to be in early development stages. The current implementation includes:
- ✅ Basic window setup and GUI framework
- ✅ Player interface definition
- ✅ 3D coordinate system
- ✅ Game logic implementation
- ✅ AI player implementation 
- ⚠️ Game rendering (placeholder)

## Future Enhancements

Potential areas for development:
1. **AI Players**: Create intelligent computer opponents using algorithms other than minimax
2. **Enhanced Graphics**: Improve the visual representation of the 3D board
3. **Game Modes**: Add different difficulty levels and game variations
4. **Player Management**: Implement human vs AI, AI vs AI, and human vs human modes
5. **Save/Load**: Add functionality to save and resume games

## Contributing

This project provides a solid foundation for building a 3D Tic Tac Toe game. Contributions are welcome for:
- Implementing better game logic
- Creating new AI players
- Improving the user interface
- Adding new features

## License

This project is open source and available for educational and development purposes.
