package prjageda;

public class PrjAGEDA {

    //Arquivo com Atributos Numéricos
    private static final String arquivo = "C:\\ArffTeste\\ionosphere.arff";

    //Arquivo com Atributos Nominais
    //private static final String arquivo = "C:\\ArffTeste\\weathernominal.arff";
    public static void main(String[] args) throws Exception, Throwable {
        //--------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        //01° - Leitura do(s) Arquivo(s) <nome(s)>.arff
        //02° - Processamento das Instâncias (Decision Stumps) - Criação das Árvores com seus atributos { Numérico - Bifurcado / Demais - Quantidade de atributos existentes }
        //03° - Geração das Árvore e das Sub-Árvores
        //    - Sorteia-se o nodo raiz de forma aleatória
        //    - Percorrer até atingir o nível de profundidade definido por parâmetro
        //    - Para cada atributos da Árvore será sorteado c/ 50% de probabilidade a inserção de uma nova Sub-Árvore
        //    - Retornar a Árvore com todas as suas Sub-Árvores
        //04° - Geração da População Inicial até atingir a quantidade de Árvores parametrizada
        //05° - Efetuado o Treinamento das Árvores (para definição das classes dos nós folhas E da classe dominante de cada um dos nodos folhas)
        //06° - Efetuado a Avaliação das Árvores 
        //07° - Cálculo do Fitness de todas as Árvores - Somatório das ocorrências DIVIDINDO-SE pelo Total de Instâncias avaliadas (Cálculo pelo Indice Gini)
        //08° - Ordenação da População Crescentemente (0.2, 0.3, 0.4 ...)
        //09° - Utilização do Elitismo p/ a geração futura aonde sempre leva-se os 2 melhores Árvores da geração atual
        //10° - Crossover - Sorteia-se 2 Árvores Aleatóriamente e efetua-se a troca genética entre elas criando 2 Novas Árvores
        //11° - Mutação - Percorre-se todos os árvores da nova geração(exceto as 2 primeiras devido ao Elitismo) e sorteando um valor, e se o mesmo estiver no intervalo 
        //      estabelecido efetua o processo que consiste na troca material genético da própria Árvore, que poderá ser:
        //      - "E"xpansão - Adiciona-se alguma das Decisions Stumps (sorteada aleatóriamente) dentre as existentes
        //      - "R"edução  - Transforma-se um nodo e todas as suas sub-árvores em um nodo folha
        //12° - Repete-se a partir do passo 07° Novamente até atingir o nro de Gerações
        //13° - Retorna a melhor árvore
        //--------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        new AlGEnArDe().AlGenArDe(new Processamento(arquivo).lerArquivoDados());

    }

}
