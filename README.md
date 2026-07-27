# Hangman Simplest
//yeah

This project builds on the original base version and adds a more polished, feature-rich experience.

## Major changes compared to the original version

1. Added a full multi-screen flow
   - Introduced a menu screen, an intro screen, and a gameplay screen.
   - Added smooth scene transitions between views.

2. Added difficulty levels
   - Players can now choose Easy, Medium, or Hard.
   - Word selection is filtered by difficulty to make the game more dynamic.

3. Expanded word system
   - Replaced the single generic word list with categorized word files such as animals, colors, countries, fruits, movies, music, professions, sports, vehicles, and weather.
   - Words now include category information and better game variety.

4. Improved game logic
   - The model now tracks mistakes and victory/game-over states more clearly.
   - Spaces are automatically handled so multi-word phrases do not block progress.

5. Added richer visual and audio experience
   - Added animated video backgrounds, intro/loading videos, and try-again overlays.
   - Added sound effects for correct guesses, wrong guesses, menu actions, victory, and defeat.

6. Enhanced user interface
   - Added styled keyboard buttons, result overlays, custom fonts, and improved game-board layout.
   - Added buttons for restarting the game and returning home.

7. Added media-management support
   - Introduced helper classes for loading, preloading, and validating media resources more reliably.
   - This makes the app more robust when running media-heavy scenes.

8. Improved project structure
   - Split responsibilities across dedicated controller, model, and application helper classes for better maintainability.
