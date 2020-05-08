package Messages;

public final class MessageFactory {
    private MessageFactory() {}

    public static Object getMessage(String type)
    {
        switch(type)
        {
            case "ACF-MSG": return new AccountFailedMessage();

            case "ACS-MSG": return new AccountSuccessfulMessage();

            case "AAG-MSG": return new AllActiveGamesMessage();

            case "CNC-MSG": return new ConcedeMessage();

            case "COF-MSG": return new ConnectFailedMessage();

            case "CNT-MSG": return new ConnectToLobbyMessage();

            case "CAC-MSG": return new CreateAccountMessage();

            case "CAI-MSG": return new CreateAIGameMessage();

            case "CLB-MSG": return new CreateLobbyMessage();

            case "DAC-MSG": return new DeactivateAccountMessage();

            case "FUL-MSG": return new FullLobbyMessage();

            case "GLG-MSG": return new GameLogMessage();

            case "GRE-MSG": return new GameResultMessage();

            case "GMP-MSG": return new GamesPlayedMessage();

            case "GVW-MSG": return new GameViewersMessage();

            case "ILM-MSG": return new IllegalMoveMessage();

            case "IAG-MSG": return new InactiveGameMessage();

            case "LEM-MSG": return new LegalMoveMessage();

            case "LOF-MSG": return new LoginFailedMessage();

            case "LOG-MSG": return new LoginMessage();

            case "LOS-MSG": return new LoginSuccessfulMessage();

            case "MOV-MSG": return new MoveMessage();

            case "NAI-MSG": return new NewAILobbyMessage();

            case "NLB-MSG": return new NewLobbyMessage();

            case "SPC-MSG": return new SpectateMessage();

            case "SSP-MSG": return new StopSpectatingMessage();

            case "STS-MSG": return new StatsMessage();

            case "UPA-MSG": return new UpdateAccountInfoMessage();
        }

        return null;
    }
}
