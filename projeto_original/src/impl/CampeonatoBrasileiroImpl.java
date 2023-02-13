package impl;

import dominio.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Integer.parseInt;

public class CampeonatoBrasileiroImpl {

    private Map<Integer, List<Jogo>> brasileirao;
    private List<Jogo> jogos;
    private Predicate<Jogo> filtro;

    public CampeonatoBrasileiroImpl(Path arquivo, Predicate<Jogo> filtro) throws IOException {
        this.jogos = filtroPorAno(lerArquivo(arquivo), filtro);
        this.brasileirao = jogos.stream()
                .collect(Collectors.groupingBy(
                        Jogo::rodada,
                        Collectors.mapping(Function.identity(), Collectors.toList())));

    }

    public List<Jogo> filtroPorAno(List<Jogo> jogos, Predicate<Jogo> filtroPorAno) {
        List<Jogo> jogosFiltradosPorAno = new ArrayList<>();
        for (int i = 0; i < jogos.size(); i++) {
            if (filtroPorAno.test(jogos.get(i))) {
                jogosFiltradosPorAno.add(jogos.get(i));
            }
        }
        return jogosFiltradosPorAno;
    }

    public List<Jogo> lerArquivo(Path file) throws IOException {
        List<String> lines = Files.readAllLines(file);
        DateTimeFormatter formater = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter formaterTime = DateTimeFormatter.ofPattern("HH:mm");
        List<Jogo> jogos = new ArrayList<>();
        for (String line : lines.stream().skip(1).toList()) {
            String[] infos = line.split(";");
            Integer rodada = parseInt(infos[0]);
            if (infos[1].equalsIgnoreCase("17/05/2007")) {
                infos[1] = "17/05/2008";
            }
            LocalDate data = LocalDate.parse(infos[1].replace("2021", "2020"), formater);
            LocalTime horario = LocalTime.now();
            if (infos[2].equals("")) {
                horario = LocalTime.parse("00:00", formaterTime);
            } else {
                horario = LocalTime.parse(infos[2].replace("h", ":"), formaterTime);
            }
            DayOfWeek diaDaSemana = data.getDayOfWeek();
            DataDoJogo dataDoJogo = new DataDoJogo(data, horario, diaDaSemana);
            if (infos[4].equalsIgnoreCase("Paysandy")) {
                infos[4] = "Paysandu";
            } else if (infos[5].equalsIgnoreCase("Paysandy")) {
                infos[5] = "Paysandu";
            } else if (infos[6].equalsIgnoreCase("Paysandy")) {
                infos[6] = "Paysandu";
            }
            Time timeMandante = new Time(infos[4]);
            Time timeVisitante = new Time(infos[5]);
            Time timeVencedor = new Time(infos[6]);
            String arena = infos[7];
            Integer mandantePlacar = parseInt(infos[8]);
            Integer visitantePlacar = parseInt(infos[9]);
            String estadoMandante = infos[10];
            String estadoVisitante = infos[11];
            String estadoVencedor = infos[12];
            Jogo jogo = new Jogo(rodada, dataDoJogo, timeMandante, timeVisitante, timeVencedor, arena, mandantePlacar,
                    visitantePlacar, estadoMandante, estadoVisitante, estadoVencedor);
            jogos.add(jogo);
        }
        return jogos;
    }

    public IntSummaryStatistics getEstatisticasPorJogo() {
        return jogos.stream().mapToInt(jogo -> jogo.visitantePlacar() + jogo.mandantePlacar()).summaryStatistics();

    }

    public Map<Jogo, Integer> getMediaGolsPorJogo() {
        return null;
    }

    public IntSummaryStatistics GetEstatisticasPorJogo() {
        return null;
    }

    public List<Jogo> todosOsJogos() {
        return null;
    }

    public Long getTotalVitoriasEmCasa() {
        return jogos.stream().filter(jogo -> jogo.mandantePlacar() > jogo.visitantePlacar()).count();
    }

    public Long getTotalVitoriasForaDeCasa() {
        return jogos.stream().filter(jogo -> jogo.mandantePlacar() < jogo.visitantePlacar()).count();
    }

    public Long getTotalEmpates() {
        return jogos.stream().filter(jogo -> Objects.equals(jogo.visitantePlacar(), jogo.mandantePlacar())).collect(Collectors.counting());
    }

    public Long getTotalJogosComMenosDe3Gols() {
        return jogos.stream().filter(jogo -> jogo.visitantePlacar() + jogo.mandantePlacar() < 3).collect(Collectors.counting());
    }

    public Long getTotalJogosCom3OuMaisGols() {
        return jogos.stream().filter(jogo -> jogo.visitantePlacar() + jogo.mandantePlacar() >= 3).collect(Collectors.counting());
    }

    public Map<Resultado, Long> getTodosOsPlacares() {
        return jogos.stream()
                .collect(Collectors.groupingBy(jogo ->
                        new Resultado(jogo.mandantePlacar(), jogo.visitantePlacar()), Collectors.counting()));
    }

    public Map.Entry<Resultado, Long> getPlacarMaisRepetido() {
        return getTodosOsPlacares().entrySet().stream().max(Map.Entry.comparingByValue()).stream().toList().get(0);
    }

    public Map.Entry<Resultado, Long> getPlacarMenosRepetido() {
        return getTodosOsPlacares().entrySet().stream().min(Map.Entry.comparingByValue()).stream().toList().get(0);
    }

    public List<Time> getTodosOsTimes() {
        Map<Time, Long> mapDeTimePorLong = jogos.stream().collect(Collectors.groupingBy
                (jogo -> jogo.mandante(), Collectors.counting()));
        return mapDeTimePorLong.entrySet().stream().map(time -> time.getKey()).collect(Collectors.toList());
    }

    public Map<Time, List<Jogo>> getTodosOsJogosPorTime() {
        Map<Time, List<Jogo>> todosOsJogosPorTime = new HashMap<>();
        var jogosMandante = jogos.stream().collect(Collectors.groupingBy(jogo -> jogo.mandante()));
        var jogosVisitante = jogos.stream().collect(Collectors.groupingBy(jogo -> jogo.visitante()));
        todosOsJogosPorTime.putAll(jogosMandante);
        todosOsJogosPorTime.putAll(jogosVisitante);
        return todosOsJogosPorTime;
    }

    public Map<Time, Map<Boolean, List<Jogo>>> getJogosParticionadosPorMandanteTrueVisitanteFalse() {
        return null;
    }

    private void adicionarInformacoesDosTimes(Map<Time, List<Long>> dadosDosTimes, Map<Time, Long> metodosDeContagem, int indice) {
        for (Map.Entry<Time, Long> entrada : metodosDeContagem.entrySet()) {
            Time time = entrada.getKey();
            Long valor = entrada.getValue();

            if (!dadosDosTimes.containsKey(time)) {
                dadosDosTimes.put(time, new ArrayList<>(Collections.nCopies(8, 0L)));
            }

            dadosDosTimes.get(time).set(indice, valor);
        }
    }

    public Set<PosicaoTabela> getTabela() {
        Map<Time, List<Long>> dadosDosTimes = new HashMap<>();
        adicionarInformacoesDosTimes(dadosDosTimes, pontosPorTime(), 0);
        adicionarInformacoesDosTimes(dadosDosTimes, vitoriasPorTime(), 1);
        adicionarInformacoesDosTimes(dadosDosTimes, derrotasPorTime(), 2);
        adicionarInformacoesDosTimes(dadosDosTimes, empatesPorTime(), 3);
        adicionarInformacoesDosTimes(dadosDosTimes, golsPositivosPorTime(), 4);
        adicionarInformacoesDosTimes(dadosDosTimes, golsSofridosPorTime(), 5);
        adicionarInformacoesDosTimes(dadosDosTimes, saldoDeGolsPorTime(), 6);
        adicionarInformacoesDosTimes(dadosDosTimes, quantidadeJogosPorTime(), 7);

        Set<PosicaoTabela> tabelaOrdenada = new TreeSet<>(
                Comparator.comparingLong((PosicaoTabela posicao) -> posicao.vitorias() * 3 + posicao.empates())
                        .thenComparingLong(posicao -> posicao.vitorias())
                        .thenComparingLong(posicao -> posicao.saldoDeGols())
                        .thenComparingLong(posicao -> posicao.golsPositivos())
                        .reversed()
        );

        for (Map.Entry<Time, List<Long>> entrada : dadosDosTimes.entrySet()) {
            Time time = entrada.getKey();
            List<Long> valores = entrada.getValue();

            tabelaOrdenada.add(new PosicaoTabela(time, valores.get(0), valores.get(1), valores.get(2), valores.get(3),
                    valores.get(4), valores.get(5), valores.get(6), valores.get(7)));
        }

        return tabelaOrdenada;
    }


    public Map<Time, Long> vitoriasPorTime() {
        return jogos.stream().filter(jogo -> !jogo.vencedor().nome().equals("-"))
                .collect(Collectors.groupingBy(jogo -> jogo.vencedor(), Collectors.counting()));
    }

    public Map<Time, Long> empatesPorTime() {
        var empateMandante = jogos.stream().filter(jogo -> jogo.vencedor().nome().equals("-"))
                .collect(Collectors.groupingBy(jogo -> jogo.mandante(), Collectors.counting()));
        var empateVisitante = jogos.stream().filter(jogo -> jogo.vencedor().nome().equals("-"))
                .collect(Collectors.groupingBy(jogo -> jogo.visitante(), Collectors.counting()));
//        Map<Time, Long> empatesTime = new HashMap<>();
//        empatesTime.putAll(empateMandante);
//        empatesTime.putAll(empateVisitante);
//        return empatesTime;
        return Stream.of(empateMandante, empateVisitante).flatMap(map -> map.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Long::sum));
    }

    public Map<Time, Long> derrotasPorTime() {
        var derrotaVisitante = jogos.stream()
                .filter(jogo -> jogo.mandante().equals(jogo.vencedor()))
                .collect(Collectors.groupingBy(jogo -> jogo.visitante(), Collectors.counting()));
        var derrotaMandante = jogos.stream()
                .filter(jogo -> jogo.visitante().equals(jogo.vencedor()))
                .collect(Collectors.groupingBy(jogo -> jogo.mandante(), Collectors.counting()));
//        Map<Time, Long> derrotasTime = new HashMap<>();
//        derrotasTime.putAll(derrotaMandante);
//        derrotasTime.putAll(derrotaVisitante);
//        return derrotasTime;
        return Stream.of(derrotaVisitante, derrotaMandante).flatMap(map -> map.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Long::sum));
    }

    public Map<Time, Long> pontosPorTime() {
        return Stream.of(vitoriasPorTime().entrySet().stream()
                                .collect(Collectors.toMap(Map.Entry::getKey, vitoria -> vitoria.getValue() * 3)),
                        empatesPorTime()).flatMap(map -> map.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Long::sum));
    }

    public Map<Time, Long> quantidadeJogosPorTime() {
        return getTodosOsJogosPorTime().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, qntJogos -> Long.valueOf(qntJogos.getValue().size() * 2)));
    }

    public Map<Time, Long> golsPositivosPorTime() {
        return Stream.of(golsPositivosComoMandante(), golsPositivosComoVisitante()).flatMap(m -> m.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Long::sum));
    }

    public Map<Time, Long> golsSofridosPorTime() {
        return Stream.of(golsSofridosComoMandante(), golsSofridosComoVisitante()).flatMap(m -> m.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Long::sum));
    }

    public Map<Time, Long> golsPositivosComoVisitante() { //PRIVATE
        return jogos.stream().collect(Collectors.groupingBy(jogo -> jogo.visitante())).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().stream().mapToLong(Jogo::visitantePlacar).sum()));
    }

    public Map<Time, Long> golsPositivosComoMandante() { //PRIVATE
        return jogos.stream().collect(Collectors.groupingBy(jogo -> jogo.mandante())).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().stream().mapToLong(Jogo::mandantePlacar).sum()));

    }

    public Map<Time, Long> golsSofridosComoVisitante() {
        return jogos.stream().collect(Collectors.groupingBy(jogo -> jogo.visitante())).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().stream().mapToLong(Jogo::mandantePlacar).sum()));

    }

    public Map<Time, Long> golsSofridosComoMandante() {
        return jogos.stream().collect(Collectors.groupingBy(jogo -> jogo.mandante())).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().stream().mapToLong(Jogo::visitantePlacar).sum()));
    }

    public Map<Time, Long> saldoDeGolsPorTime() {
        return Stream.of(golsPositivosPorTime(),
                        golsSofridosPorTime().entrySet().stream()
                                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue() * -1)))
                .flatMap(m -> m.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Long::sum));
    }

    private DayOfWeek getDayOfWeek(String dia) {
        return null;
    }

    private Map<Integer, Integer> getTotalGolsPorRodada() {
        return null;
    }

    private Map<Time, Integer> getTotalDeGolsPorTime() {
        return null;
    }

    private Map<Integer, Double> getMediaDeGolsPorRodada() {
        return null;
    }


}