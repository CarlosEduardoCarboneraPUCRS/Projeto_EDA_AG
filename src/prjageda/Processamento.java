package prjageda;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class Processamento {

    //<editor-fold defaultstate="collapsed" desc="Declaração Atributos e Método(s) Construtor(es) da Classe">    
    private String caminhoDados;
    private Arvores arvTemporaria = null;
    private ArrayList<String> nomesAtrs = null;

    //Declaração de objetos
    public static final MersenneTwister mt = new MersenneTwister();

    //Configuração para mutação da árvore
    private static final double percMutacao = 0.05; //5% de Possibilidade de Mutação
    private static final int qtdSelTorneio = 10;

    public String getCaminhoDados() {
        return caminhoDados;
    }

    public void setCaminhoDados(String caminho) {
        this.caminhoDados = caminho;
    }

    public Processamento(String local) {
        //Inicialização dos atributos
        this.caminhoDados = local;

    }

    public Processamento() {

    }
    //</editor-fold>        

    //<editor-fold defaultstate="collapsed" desc="Métodos de Processamento Diversos">        
    //Leitura do Arquivo
    public Instances LeituraArquivo() {
        //Declaração Variáveis e Objetos
        Instances dados = null;

        try {
            //Inicialização da Leitura
            dados = new DataSource(caminhoDados).getDataSet();

            //Setar o atributo classe
            if (dados.classIndex() == -1) {
                dados.setClassIndex(dados.numAttributes() - 1);
            }

        } catch (Exception e) {
            //Se ocorreu alguma exceção
            System.out.println(e.getMessage());

        }

        //Definição do Retorno
        return dados;

    }

    //Processamento das instâncias lidas da base de dados
    public ArrayList<Atributos> ProcessamentoInstancias(Instances dados, int posicao) {
        //Declaração Variáveis e Objetos
        ArrayList<Atributos> registros = new ArrayList<>();

        //1 - Avaliar se o Atributo é Numérico ou Nominal 
        //  - 1.1 - Se Numérico a árvore terá 2 arestas (Árvore será bifurcada) 
        //  - 1.2 - Se Categórico terá o número de arestas em função da quantidade de atributos encontrados no dataset
        if (dados.attribute(posicao).isNumeric()) {
            //Declaração Variáveis e Objetos
            String indiceGini = String.valueOf(Arredondar(calcularIndiceGini(dados, posicao), DecisionStumps.qtdDecimais, 1));

            //Para atributo numéricos, SEMPRE será bifurcada, assim: 
            // Aresta 0 - Sempre será MENOR OU IGUAL a Média Calculada
            // Aresta 1 - Sempre será MAIOR que a Média Calculada
            registros.add(new Atributos("<= " + indiceGini, null, "", null));
            registros.add(new Atributos("> " + indiceGini, null, "", null));

        } else {
            //Percorrer a Quantidade de Atributos existentes e adicionando os mesmos
            for (int i = 0; i < dados.attribute(posicao).numValues(); i++) {
                //Adicionar as Arestas
                registros.add(new Atributos(dados.attribute(posicao).value(i), null, "", null));

            }

        }

        //Definir o retorno
        return registros;

    }

    //Efetuar o processamento Recursivo da Árvore, lendo cada instância da base de dados e percorrer toda a árvore
    public List<Arvores> NovaGeracaoArvores(Instances dados, List<Arvores> arvores, boolean elitismo) throws IOException {
        //Declaração Variáveis e Objetos
        List<Arvores> populacao = new ArrayList<>();
        List<Arvores> filhos;

        //Se tiver elitismo, adicionar (mantém) a melhor árvore da geração atual(ordenada) para a próxima geração
        if (elitismo) {
            //Adicionar as árvores obtidas por Elitismo
            populacao.add(arvores.get(0));
            populacao.add(arvores.get(1));

        }

        //Efetua a geração da nova população equanto a população for menor que a população inicialmente estabelecida
        while (populacao.size() < DecisionStumps.quantidade) {
            //Inicialização do Objeto
            filhos = new ArrayList<>();

            //Adicionar os pais
            filhos.add(SelecaoPorTorneio(arvores));
            filhos.add(SelecaoPorTorneio(arvores));

            //SE Valor Gerado <= TxCrossover, realiza o Crossover entre os pais SENÃO mantém os pais selecionados através de Torneio p/ a próxima geração            
            if (mt.nextDouble() <= DecisionStumps.TxCrossover) {
                //Adicionar os 2 filhos que sofreram Crossover
                populacao.addAll(CrossoverArvores(filhos.get(0), filhos.get(1)));
               
            } else {
                //Apenas adicionar os 2 filhos selecionados
                populacao.add(filhos.get(0));
                populacao.add(filhos.get(1));

            }

        }

        //Efetuar Mutação das Árvores (se selecionado pelo critério do %), Exceto p/ as Árvores obtidas por Eletismo
        for (int i = 2; i < populacao.size(); i++) {
            //Definir o Percentual de Mutação
            double pMutacao = Arredondar(mt.nextDouble(), 2, 1);

            //Se for MENOR OU IGUAL ao limite Superior (Valor >= 0 E Valor <= Limite Superior)
            if (pMutacao < Processamento.percMutacao) {
                //Efetuar a Mutação da Árvore - "E"xpansão ou "R"etração de Nodos
                MutacaoArvores(populacao.get(i), mt.nextBoolean() ? "E" : "R", dados, BuscarAtributosArvore(populacao.get(i)));

            }

        }

        //Definição do retorno da função
        return populacao;

    }

    //Efetuar a seleção por Torneio das Árvores - Seleciona-se as Árvores Aleatóriamente Ordenando-os Crescente
    private Arvores SelecaoPorTorneio(List<Arvores> arvores) {
        //Declaração Variáveis e Objetos
        ArrayList<Arvores> selecao = new ArrayList<>();

        //Enquanto não atingir o Tamanho Máximo
        while (selecao.size() < qtdSelTorneio) {
            //Selecionar 2 árvores aleatóriamente
            selecao.add(arvores.get(mt.nextInt(arvores.size() - 1)));

        }

        //Ordenar a População
        Collections.sort(selecao);

        //Retornar o melhor Individuo
        return selecao.get(0);

    }

    //Efetuar o crossover da população de árvores, aonde ocorre a Troca Genética de Material entre as árvores CRIANDO novas árvores
    private ArrayList<Arvores> CrossoverArvores(Arvores arv1, Arvores arv2) {
        //Declaração Variáveis e Objetos e Inicializações
        ArrayList<Arvores> populacao = new ArrayList<>();

        //Buscar um dos atributos selecionados aleatóriamente e Remover Sub-Árvore da 1° Árvore transformando em um nodo folha na respectiva posição 
        RemoverNodoNaOrigemSetandoNuloERetornandoArvore(arv1, BuscarAtributosArvore(arv1));

        //Incluir o nodo removido na 1° Árvore em uma posição aleatório da 2° Árvore (desde que a posição seja um nodo folha)
        PesquisarPosicaoArvoreDestino(arv2);

        //Buscar um dos atributos selecionados aleatóriamente e Remover Sub-Árvore da 2° Árvore transformando em um nodo folha na respectiva posição 
        RemoverNodoNaOrigemSetandoNuloERetornandoArvore(arv2, BuscarAtributosArvore(arv2));

        //Incluir o nodo removido na 1° Árvore em uma posição aleatória da 2° Árvore (desde que a posição seja um nodo folha)
        PesquisarPosicaoArvoreDestino(arv1);

        //Adicionar as árvores c/ o "CROSSOVER" efetuado e limpar o objeto
        populacao.add(arv1);
        populacao.add(arv2);

        //Liberar o Objeto
        arvTemporaria = null;

        //Definir o retorno
        return populacao;

    }

    //</editor-fold>        
    //<editor-fold defaultstate="collapsed" desc="Funções Pertinentes aos Métodos de Mutação">   
    //Efetuar a Mutação da árvore, a mesma poderá ser de "E"xpansão ou "R"edução
    private void MutacaoArvores(Arvores arvore, String tipo, Instances dados, String nomeAtr) throws IOException {
        //Se a árvore não for nula
        if (arvore != null) {
            //SE for "E" - EXPANSÃO - Vai até um nodo FOLHA ALEATÓRIO E ADICIONA um DecisionStumps Aleatóriamente
            //SENÃO  "R" - REDUÇÃO  - Vai até o nodo passado como parâmetro e transforma-se todos as sub-árvores abaixo em folhas
            if (tipo.equals("E")) {
                //Se possuir arestas válidas
                if (arvore.getArestas() != null) {
                    //Declaração Variáveis e Objetos - Selecionar uma posição aleatória
                    int posicao = mt.nextInt(arvore.getArestas().size() - 1);

                    //Se a aresta selecionada não for nula
                    if (arvore.getArestas(posicao).getNodo() != null) {
                        //Se atingiu o MAIOR NÍVEL de profundidade da árvore (O último nodo da aresta DEVERÁ SER nulo) - Avaliando a aresta SELECIONADA aleatóriamente
                        if (arvore.getArestas(posicao).getNodo().getArestas(posicao).getNodo() == null) {
                            //Declaração Variáveis e Objetos
                            ArrayList<Arvores> temp = new DecisionStumps().LeituraNodos();

                            //Selecionar aleatóriamente uma árvore p/ ser incluida no nodo raiz e Adicionar o nodo na aresta selecionada
                            arvore.getArestas(posicao).getNodo().getArestas(posicao).setNodo(temp.get(Processamento.mt.nextInt(temp.size() - 1)));

                        } else {
                            //Chamar a função recursivamente até chegar em um nodo raiz
                            MutacaoArvores(arvore.getArestas(posicao).getNodo(), tipo, dados, nomeAtr);

                        }

                    } else {
                        //Declaração Variáveis e Objetos
                        ArrayList<Arvores> temp = new DecisionStumps().LeituraNodos();

                        //Se o nodo for nulo, seleciona uma Decision Stump aleatóriamente p/ ser incluida no nodo folha
                        arvore.getArestas(posicao).setNodo(temp.get(Processamento.mt.nextInt(temp.size() - 1)));

                    }

                }

            } else {
                //Se possuir arestas válidas
                if (arvore.getArestas() != null) {
                    //Percorrer todas as arestas
                    for (int i = 0; i < arvore.getArestas().size(); i++) {
                        //Se o nodo não for nulo
                        if (arvore.getArestas(i).getNodo() != null) {
                            //Se for o Atributo Selecionado Atribuo nulo senão retorno pra pesquisa
                            if (arvore.getArestas(i).getNodo().getNomeAtributo().equals(nomeAtr)) {
                                //Transformar o Nodo c/ arestas em Nodo Folha (Mutação de "REDUÇÃO") E Sair do processamento
                                arvore.getArestas(i).setNodo(null);
                                break;

                            } else {
                                //Chamada recursiva da função atualizando o nível de profundidade
                                MutacaoArvores(arvore.getArestas(i).getNodo(), tipo, dados, nomeAtr);

                            }

                        }

                    }

                }

            }

        }

    }
    //</editor-fold> 

    //<editor-fold defaultstate="collapsed" desc="Funções Destinadas ao Crossover">
    //Remover uma Sub-Árvore da Árvore atual e setar nulo a mesma
    public void RemoverNodoNaOrigemSetandoNuloERetornandoArvore(Arvores arvore, String nomeAtr) {
        try {
            //Se possuir arestas
            if (arvore.getArestas() != null) {
                //Percorre todas as arestas até encontrar o atributos selecionado
                for (int i = 0; i < arvore.getArestas().size(); i++) {
                    //Se o nodo não for nulo
                    if (arvore.getArestas(i).getNodo() != null) {
                        //Se for o nodo da aresta selecionado aleatóriamente
                        if (arvore.getArestas(i).getNodo().getNomeAtributo().equals(nomeAtr)) {
                            //Criar o novo objeto e atribuir o mesmo
                            arvTemporaria = new Arvores();
                            arvTemporaria = arvore.getArestas(i).getNodo();

                            //Setar nulo p/ a sub-árvore selecionada
                            arvore.getArestas(i).setNodo(null);

                            //Sair do for (Setar a posição máxima)
                            break;

                        } else {
                            //Chamar Recursivamente a função até encontrar o nodo
                            RemoverNodoNaOrigemSetandoNuloERetornandoArvore(arvore.getArestas(i).getNodo(), nomeAtr);

                        }

                    }

                }

            }

        } catch (Exception e) {
            System.out.println(e.getMessage());

        }

    }

    //Pesquisar um nodo folha da árvore de destino p/ inserção da Sub-Árvore
    public void PesquisarPosicaoArvoreDestino(Arvores arvore) {
        try {
            //Se o nó não for nulo
            if (arvore != null) {
                //Se possuir arestas
                if (arvore.getArestas() != null) {
                    //Selecionar uma posição aleatóriamente (Não importa qual, pois é inclusão de um novo nodo)
                    int posicao = mt.nextInt(arvore.getArestas().size() - 1);

                    //Se o Nodo da Aresta não for nula, existem Sub-Árvores
                    if (arvore.getArestas(posicao).getNodo() != null) {
                        //Chamada Recursiva da Função p/ Avaliação da Sub-Árvore informada
                        PesquisarPosicaoArvoreDestino(arvore.getArestas(posicao).getNodo());

                    } else {
                        //Se a aresta informada for nula insere o nodo e finaliza o ciclo
                        arvore.getArestas(posicao).setNodo(arvTemporaria);

                    }

                }

            }
        } catch (Exception e) {
            System.out.println(e.getMessage());

        }

    }

    //Atribui ao nodo folha a(s) classe(s) a qual pertence, se não existe atribui a mesma senão atualiza a quantidade em um unidade(caso exista a mesma)
    public void AtribuicaoClasseNodosFolhas(Arvores arvore, Instance avaliacao) {
        //Se o árvore não for nula
        if (arvore != null) {
            //Percorrer todos os atributos da instância selecionada (Exceto o atributo Classe)
            for (int k = 0; k < avaliacao.numAttributes(); k++) {
                //Se o nome do Atributo Classe for igual ao nome do atributo da instância (Raiz ou nodo folha)
                if (avaliacao.attribute(k).name().equals(arvore.getNomeAtributo())) {
                    //Se o atributo for Numérico (BIFURCAÇÃO)
                    if (avaliacao.attribute(k).isNumeric()) {
                        //Declaração Variáveis e Objetos
                        double valorAresta = Double.valueOf(arvore.getArestas(0).getAtributo().split(" ")[1]);

                        //Se valor posição 0 FOR MENOR IGUAL ao valor atributo selecionado (Então posição igual a 0 SENAO 1)
                        int posicao = Arredondar(avaliacao.value(k), DecisionStumps.qtdDecimais, 1) <= Arredondar(valorAresta, DecisionStumps.qtdDecimais, 1) ? 0 : 1;

                        //Se não for um nodo RAIZ, efetua a chamada recursiva da função até chegar em um nodo raiz
                        if (arvore.getArestas(posicao).getNodo() != null) {
                            //Chama a função recursivamente passando o nodo da aresta
                            AtribuicaoClasseNodosFolhas(arvore.getArestas(posicao).getNodo(), avaliacao);

                        } else {
                            //Declaração Variáveis e Objetos
                            ArrayList<Classes> classes = new ArrayList<>();

                            //Se a Classe for vazia Inclui o mesmo (Sendo o 1° Registro)
                            if (arvore.getArestas(posicao).getClasses() == null) {
                                //Adicionar a Nova classe 
                                classes.add(new Classes(avaliacao.classAttribute().value((int) avaliacao.classValue()), 1));

                                //Atribuir as classes e sair fora da execução para a aresta selecionada
                                arvore.getArestas(posicao).setClasses(classes);

                            } else //Já Existem Registros na Classe, irá atualizar o mesmo
                            {
                                //Declaração Variáveis e Objetos
                                boolean bOk = false;
                                classes = arvore.getArestas(posicao).getClasses();

                                //Percorre TODAS as classes do Nodo
                                for (Classes classe : classes) {
                                    //Se o valor da aresta FOR IGUAL AO VALOR DO ATRIBUTO DA INSTÂNCIA                                            
                                    //Se o "VALOR" da classe DA INSTÂNCIA SELECIONADA FOR IGUAL a da classe informada atualiza a quantidade
                                    if (avaliacao.classAttribute().value((int) avaliacao.classValue()).equals(classe.getNome())) {
                                        //Atualizar a quantidade (Adicionando 1) de registros X Atributo - Para Definir a Classe dominante
                                        classe.atualizarQtd(1);
                                        bOk = true;

                                    }

                                }

                                //Se não existe o atributo inclui o mesmo
                                if (!bOk) {
                                    //Se não for nulo (ser o último)
                                    if (avaliacao.classAttribute() != null) {
                                        //Adicionar a Nova classe e atualizar a quantidade
                                        classes.add(new Classes(avaliacao.classAttribute().value((int) avaliacao.classValue()), 1));

                                    }

                                }
                                //Atribuir as classes e sair fora da execução para a aresta selecionada
                                arvore.getArestas(posicao).setClasses(classes);

                            }

                        }
                    } else { //Se for categórico
                        if (arvore.getArestas() != null) {
                            //Percorrer todas as arestas da arvore
                            for (int i = 0; i < arvore.getArestas().size(); i++) {
                                //Declaração Variáveis e Objetos
                                ArrayList<Classes> classes = arvore.getArestas(i).getClasses();

                                //Percorrer todas as classes da Aresta
                                for (Classes classe : classes) {
                                    //Percorrer todos os valores existentes da instância selecionada
                                    for (int l = 0; l < avaliacao.numValues(); l++) {
                                        //Se o nome do Atributo FOR IGUAL AO NOME DO ATRIBUTO da instancia de avaliação selecionada
                                        if (arvore.getArestas(i).getAtributo().equals(avaliacao.attribute(k).value(l))) {
                                            //Se o nome da classe dominante for igual a classe avaliada
                                            if (classe.getNome().equals(avaliacao.classAttribute().value((int) avaliacao.classValue()))) {
                                                //Atualizar a quantidade de registros X Atributo - Para Definir a Classe dominante
                                                classe.atualizarQtd(1);

                                            }

                                        }

                                    }

                                }

                            }
                        }

                    }
                    //Sair fora do for
                    break;

                }

            }

        }

    }

    //Irá percorrer todos os Nodos da árvore(avaliando SOMENTE os nodos folhas) 
    public void DefinicaoClasseMajoritariaNodosFolhas(Arvores arvore) {
        //Se o árvore não for nula
        if (arvore.getArestas() != null) {
            //Percorrer TODAS as arestas do árvore selecionado para atribuir uma classe as folhas
            for (int i = 0; i < arvore.getArestas().size(); i++) {
                //Se a aresta selecionada não for NULA pesquisa pela mesma (NULA == Nodo Folha)
                if (arvore.getArestas(i).getNodo() != null) {
                    //Chamada recursiva da função passando como parâmetros a aresta selecionada
                    DefinicaoClasseMajoritariaNodosFolhas(arvore.getArestas(i).getNodo());

                } else //Chegou em um nodo folha
                {
                    //Declaração Variáveis e Objetos
                    ArrayList<Classes> classes = arvore.getArestas(i).getClasses();

                    //Se não for nulo
                    if (classes != null) {
                        //Declaração Variáveis e Objetos
                        String NmClasseMaj = "";
                        double qtdClasse = 0;

                        //Percorre todas as Classes
                        for (Classes classe : classes) {
                            //Se for a 1° Ocorrência
                            if (NmClasseMaj.equals("")) {
                                //Atribuições do nome da classe e da quantidade
                                NmClasseMaj = classe.getNome();
                                qtdClasse = classe.getQuantidade();

                            } else {
                                //Se a quantidade for MAIOR que a ATUAL ALTERA a classe SENÃO mantém a mesma
                                if (classe.getQuantidade() > qtdClasse) {
                                    //Atribuições do nome da classe e da quantidade
                                    NmClasseMaj = classe.getNome();
                                    qtdClasse = classe.getQuantidade();

                                }

                            }

                        }

                        //Setar a classe Majoritária
                        arvore.getArestas(i).setClasseDominante(NmClasseMaj);

                    }

                }

            }

        }

    }
    //</editor-fold>        

    //<editor-fold defaultstate="collapsed" desc="Calcular o Valor Médio da Árvores(arestas) - Para Atributos Numéricos">    
    //Efetuar o Cálculo do Indice Gini p/ Atributos Contínuos 
    //Por Exemplo Indice Gini = 1 - (Somatório Quant. Atrib. "A" / Total de Instâncias) ^ 2 - (Somatório Quant. Atrib. "N" / Total de Instâncias) ^ 2.
    private double calcularIndiceGini(Instances dados, int pos) {
        //Declaração Variáveis e objetos
        double indiceGini = 1d, menorValor = 0d, maiorValor = 0d;

        //Pegar distintamente os valores p/ calcular a média de todas as instâncias do atributo informado        
        //Adicionar os valores das instâncias na posição informada
        for (int i = 0; i < dados.numInstances(); i++) {
            //Se for menor que o menor valor
            if (dados.instance(i).value(pos) < menorValor) {
                menorValor = dados.instance(i).value(pos);
            }

            //Se for maior que o maior valor
            if (dados.instance(i).value(pos) > maiorValor) {
                maiorValor = dados.instance(i).value(pos);
            }

        }

        //Se for a primeira posição ou a última (poderá ser negativa)
        double media = Arredondar((menorValor + maiorValor) / 2, DecisionStumps.qtdDecimais, 1);

        //Se a média for válida(maior que 0)
        if (media > 0) {
            //Como a árvore será bifurcada percorrer 2X a lista
            for (int i = 0; i < 2; i++) {
                //Declaração Variáveis e objetos
                int qtdOcor = 0;

                //Percorer todas as instâncias
                for (int j = 0; j < dados.numInstances(); j++) {
                    //Se for MENOR OU IGUAL a média ou MAIOR que a média
                    qtdOcor += (i == 0) ? (dados.instance(j).value(pos) <= media ? 1 : 0) : (dados.instance(j).value(pos) > media ? 1 : 0);

                }

                //Subtração da Potenciação ao Quadrado da Média Calculada
                indiceGini -= Math.pow(((double) qtdOcor / dados.numInstances()), 2);

            }

        }

        //Definir o Retorno (se o Índice Gini for "exatamente" 1 deverá ser 
        return Arredondar(indiceGini == 1 ? 0 : indiceGini, DecisionStumps.qtdDecimais, 1);

    }

    // Parâmetros: 1 - Valor a Arredondar. 
    //             2 - Quantidade de casas depois da vírgula. 
    //             3 - Arredondar para cima ou para baixo?
    // Para Cima  = 0 (ceil) 
    // Para Baixo = 1 ou qualquer outro inteiro (floor)
    double Arredondar(double valor, int casas, int ACimaouABaixo) {
        //Declaração Variáveis e objetos
        double arredondado = valor;

        //Atribuições do Cálculo
        arredondado *= (Math.pow(10, casas));
        arredondado = (ACimaouABaixo == 0 ? Math.ceil(arredondado) : Math.floor(arredondado));
        arredondado /= (Math.pow(10, casas));

        //Definir o Retorno
        return arredondado;

    }

    private String BuscarAtributosArvore(Arvores arv) {
        //Declaração Variáveis e Objetos
        nomesAtrs = new ArrayList<>();

        //Processar os atributos existentes na árvore
        processarNomesAtributos(arv);

        //Não poderá retornar o nodo raiz, por isto a exclusão do nodo na posição 0
        return nomesAtrs.get(mt.nextInt(nomesAtrs.remove(0).length()));

    }

    //Localizar os atributos(nomes) existentes na árvore, aonde será sorteado um deles para mutação (EXCETO o nodo Raiz)
    private void processarNomesAtributos(Arvores arv) {
        //Percorrer todas as arestas
        for (int i = 0; i < arv.getArestas().size(); i++) {
            //Se não contiver o atributo
            if (!nomesAtrs.contains(arv.getNomeAtributo())) {
                //Adicionar o nome do Atributo SE não Contiver
                nomesAtrs.add(arv.getNomeAtributo());

            }

            //Se o nodo não for nulo
            if (arv.getArestas(i).getNodo() != null) {
                //Chamada recursiva da árvore
                processarNomesAtributos(arv.getArestas(i).getNodo());

            }

        }

    }
    //</editor-fold>        

}
