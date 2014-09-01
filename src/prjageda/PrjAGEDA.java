package prjageda;

public class PrjAGEDA {

    //Arquivo com Atributos Numéricos
    private static final String arquivo = "C:\\ArffTeste\\ionosphere.arff";

    //Arquivo com Atributos Nominais
    //private static final String arquivo = "C:\\ArffTeste\\weathernominal.arff";
    
    public static void main(String[] args) throws Exception {        
        //--------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        //01° - Leitura do(s) Arquivo(s) <nome(s)>.arff
        //02° - Processamento das Instâncias - Criação das Árvores com seus atributos { Numérico - Bifurcado / Demais - Quantidade de atributos existentes }
        //03° - Geração das Árvore e das suas Sub-Árvores
        //    - Sorteia-se o nodo raiz de forma aleatória
        //    - Percorrer até atringir o nível de profundidade definido por parâmetro
        //    - Para cada atributos da Árvore será sorteado c/ 50% de probabilidade a inserção de uma nova Sub-Árvore - ESTA COMENTADO PARA GERACAO TOTAL (TODAS AS ARESTAS)
        //    - Retornar a Árvore com todas as suas Sub-Árvores
        //04° - Geração das População Inicial até atingir a quantidade de indivíduos parametrizados
        //05° - Efetuado o Treinamento das árvores (para definição dos atributos classes dos nós folhas)
        //06° - Efetuado a Avaliação das árvores (para cálculo das quantidade das ocorrências de determinado atributo dos nós folhas)
        //07° - Cálculo do Fitness de todas as Árvores - Somatório das ocorrências DIVIDINDO-SE pelo Total de Instâncias avaliadas
        //08° - Ordenação da população crescentemente (0.2, 0.3, 0.4 ...)
        //09° - Utilização do Elitismo p/ a geração futura aonde sempre leva-se os 2 melhores indivíduos da geração atual
        //10° - Crossover - Sorteia-se 2 Indivíduos e efetua-se a troca genética entre eles Criando 2 Novos indivíduos
        //11° - Mutação - Percorre-se todos os indivíduos da nova geração(exceto os 2 primeiros devido ao elitismo) e sorteando um valor entre 1% e 5%, caso o mesmo esteja contido
        //      no intervalo efetua o processo que consiste na troca material genético do próprio individuo, que poderá ser:
        //      - Expansão - Adiciona-se algum dos decisions stumps (sorteado aleatóriamente) dentre os criados
        //      - Redução  - Transforma-se um nodo e todas as suas sub-árvores em um nodo folha
        //12° - Repete-se o item 07°
        //13° - Ordena-se a população em ordem crescente selecionando-se os 2 primeiros e leva-se p/ a próxima geração repete-se o ciclo até atingir o número máximo de gerações
        //--------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        new DecisionStumps().AlGenArDe(new Processamento(arquivo).LeituraArquivo());

    }
    
}
