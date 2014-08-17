package prjageda;

import java.util.Random;
import weka.core.Instances;
import java.util.ArrayList;
import java.util.Collections;

public class DecisionStumps {

    //<editor-fold defaultstate="collapsed" desc="Atributos da classe e Métodos Construtores da classe">    
    private static final int profundidade = 5;
    public static final int quantidade = 10;
    public static final double TxCrossover = 0.3;
    private static final int geracoes = 10;
    private ArrayList<Arvores> arvores;

    public DecisionStumps() {
    }
    //</editor-fold> 

    //<editor-fold defaultstate="collapsed" desc="Métodos de Processamento">
    public void ProcessamentoDS(Instances dados) throws Exception {
        try {
            //Declaração Objetos
            int geracao = 1;
            
            //Inicialização do objeto
            arvores = new ArrayList<>();

            //Efetuar a Geração da População Inicial
            arvores.addAll(GeracaoPopulacaoInicial(dados));

            //Ordenação da população
            //Laço até o critério de parada ser atingido
            while (geracao < geracoes) {
                //Atualizar o número de Gerações
                geracao++;

                //Cria nova populacao, utilizando o elitismo
                arvores = new Processamento().NovaGeracaoArvores(dados, arvores, true);

            }

        } catch (Exception e) {
            throw e;

        }

    }

    //<editor-fold defaultstate="collapsed" desc="Função Comentada de Recursidade para geração dos Indivíduos">    
    public void GerarPopulacaoIndividuos(Instances dados, int prof, Arvores arvore) {
        //Condição de Parada, Se o grau de profundidade for igual ao informado
        if (prof <= profundidade) {
            //percorrer todas as arestas do indivíduo
            for (int i = 0; i < arvore.getArestas().size(); i++) {
                //Processar Sim ou Não { Inserir Sub-Árvore } - c/ 50% de Probabilidade 
                if (new Random().nextBoolean()) {
                    //Declaração variáveis e Objetos
                    ArrayList<Arvores> nodos = new ArrayList<>();

                    //Adicionar os nodos originais, isto é devido ao java trabalhar APENAS com a referência dos mesmos
                    nodos.addAll(ProcessamentoNodos(dados));

                    /*
                     1°) Sortear um Nodo(Árvore) Qualquer Aleatóriamente p/ Inserção                   
                     2°) Inserir na aresta a Árvore Selecionada Aleatóriamente(No Atributo Nodo)
                     */
                    arvore.SetNodo(arvore.getArestas(i), nodos.get(new Random().nextInt(nodos.size())));

                    //Chamada Recursiva para Geração da árvore atualizando o nivel de profundidade
                    GerarPopulacaoIndividuos(dados, prof + 1, arvore.getArvoreApartirAresta(i));

                }

            }

        }

    }

    private ArrayList<Arvores> ProcessamentoNodos(Instances dados) {
        //Declaração Variáveis e Objetos
        ArrayList<Arvores> nodos = new ArrayList<>();

        /*
         Processamento: PARA CADA COLUNA PERCORRE TODAS AS LINHAS
         ---------------------------------------------------------------------------------------------------------------------------------------------------
         Percorrer TODOS os atributos (colunas) existentes e para cada atributo percorre todas as instâncias
         por exemplo: Atributo 0, Atributo 1, Atributo 2,...Atributo N-1
         */
        for (int i = 0; i < dados.numAttributes(); i++) {
            //1° Passo - Processar todos os Atributos (Binários e Nominais)
            // - 1° Parâmetro - Nome do atributo
            // - 2° Parâmetro - Instâncias e a posição do Atributo
            nodos.add(new Arvores(dados.instance(0).attribute(i).name(), 1, new Processamento().ProcessamentoInstancias(dados, i)));

        }

        //Definir o retorno
        return nodos;

    }
    //</editor-fold> 

    /*
     Geração da População Inicial
     ---------------------------------------------------------------------------------------------------------------------------------------------------
     - Cada Individuo da população será uma Árvore c/ Sub-Árvores
     - Esta possibilidade será para cada um dos atributos existentes
     */
    private ArrayList<Arvores> GeracaoPopulacaoInicial(Instances dados) {
        //Declaração Variáveis e Objetos
        ArrayList<Arvores> temp;

        //Percorrer a quantidade de Individuos
        for (int i = 0; i < quantidade; i++) {
            /*
             Adicionar o Indivíduo - Árvore Gerada c/ Sub-Arvores, aonde o nível Pai é selecionado aleatóriamente
                
             Definição dos Parâmetros
             -----------------------------------------------------------------------------------------------------------------------------------------------
             1° Parâmetro - Nivel atual de profundidade da Árvore
             2° Parâmetro - Lista de Nodos Disponíveis para Sorteio
             3° Parâmetro - Nodo Sorteado p/ ser o Nodo Raiz
             */
            temp = new ArrayList<>();
            temp.addAll(ProcessamentoNodos(dados));

            //Declaração Objetos
            Arvores arvore = temp.get(new Random().nextInt(dados.numAttributes() - 1));

            //Chamada da função para a geração dos indivíduos
            GerarPopulacaoIndividuos(dados, 1, arvore);

            //Adicionar o Indivíduo novo
            arvores.add(arvore);
            
            //Ordenar a população
            ordenaPopulacao();

        }

        //Definir o retorno
        return arvores;

    }

    /*Ordena a população pelo valor de aptidão de cada indivíduo, do maior valor
     para o menor, assim se eu quiser obter o melhor indivíduo desta população, 
     acesso a posição 0 do array de indivíduos*/
    public void ordenaPopulacao() {
        //Ordernar os registros crescente
        Collections.sort(arvores);

    }

}
