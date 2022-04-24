package chess.controller;

import chess.domain.Command;
import chess.domain.piece.Team;
import chess.service.ChessService;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class SpringController {

    private final ChessService chessService;

    public SpringController(ChessService chessService) {
        this.chessService = chessService;
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/start")
    public String start(@RequestParam("game_name") String gameName) {
        return "redirect:/game/" + gameName;
    }

    @GetMapping("/game/{gameName}")
    public String game(@PathVariable String gameName, @RequestParam(value = "error", required = false) String error, Model model) {
        List<String> chessBoard = chessService.findByName(gameName);

        model.addAttribute("chessboard", chessBoard);
        model.addAttribute("gameName", gameName);
        model.addAttribute("error", error);

        return "chess";
    }

    @PostMapping("/game/{gameName}/move")
    public String move(@PathVariable String gameName,
                       @RequestParam("from") String from, @RequestParam("to") String to,
                       Model model, RedirectAttributes redirectAttributes) {
        try {
            String command = makeCommand(from, to);
            chessService.move(command);
            if (chessService.isEnd()) {
                return "redirect:/game/" + gameName + "/end";
            }
        } catch (IllegalArgumentException e) {
            redirectAttributes.addAttribute("error", e.getMessage());
        }

        model.addAttribute("gameName", gameName);
        return "redirect:/game/" + gameName;
    }

    @GetMapping("/game/{gameName}/end")
    public String end(@PathVariable String gameName, Model model) {
        String winTeamName = chessService.finish(Command.from("end"));
        List<String> chessBoard = chessService.getCurrentChessBoard();

        model.addAttribute("winTeam", winTeamName);
        model.addAttribute("chessboard", chessBoard);
        model.addAttribute("gameName", gameName);

        return "chess";
    }

    @GetMapping("/game/{gameName}/status")
    public String status(@PathVariable String gameName, Model model) {
        Map<Team, Double> score = chessService.getScore();
        List<String> chessBoard = chessService.getCurrentChessBoard();

        model.addAttribute("blackScore", score.get(Team.BLACK));
        model.addAttribute("whiteScore", score.get(Team.WHITE));
        model.addAttribute("chessboard", chessBoard);
        model.addAttribute("gameName", gameName);

        return "chess";
    }

    @GetMapping("/error")
    public String error() {
        return "error";
    }

    private String makeCommand(String from, String to) {
        return "move " + from + " " + to;
    }
}
