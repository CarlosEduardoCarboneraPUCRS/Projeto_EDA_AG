package prjageda;

public class PrjAGEDA {

    //Arquivo com Atributos Numéricos
    private static final String arquivo = "C:\\ArffTeste\\ionosphere.arff";

    //Arquivo com Atributos Nominais
    //private static final String arquivo = "C:\\ArffTeste\\weathernominal.arff";
    
    public static void main(String[] args) throws Exception {
        /*
         1° - Leitura do(s) Arquivo(s) <nome(s)>.arff
         2° - Processamento das Instâncias - Criação das Árvores com seus atributos e as Sub-Árvores Nulas
         3° - Geração das Sub-Árvores
            - Percorrer até montar a quantidade de níveis informado
            - Para cada atributos da Árvore sortear c/ 50% de probabilidade a inserção de uma nova Sub-Árvores
            - Retornar a Árvore com todas as suas Sub-Árvores
         4° - Geração das População Inicial até atingir 1000 indivíduos criados 
        */
        new DecisionStumps().ProcessamentoDS(new Processamento(arquivo).LeituraArquivo());

    }
    
}
