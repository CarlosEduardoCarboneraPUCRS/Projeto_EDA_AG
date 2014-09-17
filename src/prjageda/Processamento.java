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

    //Declaração de objetos
    public static final MersenneTwister mt = new MersenneTwister();
    private static final double percMutacao = 0.05; //5% de Possibilidade de Mutação
    private static final int qtdElitismo = 2;

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
            String indiceGini = String.valueOf(Arredondar(calcularIndiceGini(dados, posicao), AlGEnArDe.qtdDecimais, 1));

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
    public ArrayList<Arvores> NovaGeracaoArvores(Instances dados, boolean elitismo) throws IOException, Exception {
        //Declaração Variáveis e Objetos
        ArrayList<Arvores> populacao = new ArrayList<>();
        ArrayList<Arvores> filhos;
        List<Arvores> listagem;

        try {
            //Se tiver elitismo, adicionar (mantém) a melhor árvore da geração atual(ordenada) para a próxima geração
            if (elitismo) {
                //Adicionar as árvores obtidas por Elitismo
                populacao.add((Arvores) ObjectUtil.deepCopyList(AlGEnArDe.arvores).get(0));
                populacao.add((Arvores) ObjectUtil.deepCopyList(AlGEnArDe.arvores).get(1));

            }

            //Efetua a geração da nova população equanto a população for menor que a população inicialmente estabelecida
            while (populacao.size() < AlGEnArDe.quantidade) {
                //Efetuar o DeepCopy das Árvores(JÁ QUE TUDO NO JAVA É POR REFERÊNCIA) 
                listagem = new ArrayList<>(ObjectUtil.deepCopyList(AlGEnArDe.arvores));

                //Inicialização do Objeto            
                filhos = new ArrayList<>();

                //Adicionar as Árvores pais
                filhos.add((Arvores) SelecaoPorTorneio(listagem));
                filhos.add((Arvores) SelecaoPorTorneio(listagem));

                //SE Valor Gerado <= TxCrossover, realiza o Crossover entre os pais SENÃO mantém os pais selecionados através de Torneio p/ a próxima geração            
                if (mt.nextDouble() <= AlGEnArDe.TxCrossover) {
                    //Adicionar os 2 filhos que sofreram Crossover
                    populacao.addAll(CrossoverArvores(filhos.get(0), filhos.get(1)));

                } else {
                    //Apenas adicionar os 2 filhos selecionados
                    populacao.add(filhos.get(0));
                    populacao.add(filhos.get(1));

                }

            }

            //Efetuar Mutação das Árvores (se selecionado pelo critério do %), Exceto p/ as Árvores obtidas por Elitismo
            for (int i = qtdElitismo; i < populacao.size(); i++) {
                //Se for MENOR OU IGUAL ao Limite Superior (Valor < Limite Superior)
                if (Arredondar(mt.nextDouble(), 2, 1) < Processamento.percMutacao) {
                    //Declaração Variáveis e Objetos
                    String atributo = BuscarAtributosArvore(populacao.get(i));

                    //Caso a árvore possuir mais do que o nodo raiz, retornará um atributo, caso contrário nem processa
                    if (!atributo.isEmpty()) {
                        //Efetuar a Mutação da Árvore - "E"xpansão ou "R"etração de Nodos
                        MutacaoArvores(populacao.get(i), mt.nextBoolean() ? "E" : "R", dados, atributo, 0);

                    }

                }

            }

        } catch (Exception e) {
            System.out.println(e.getMessage());

        }

        //Definição do retorno da função
        return populacao;

    }

    //Efetuar a seleção por Torneio das Árvores - Seleciona-se as Árvores Aleatóriamente Ordenando-os Crescente
    private Arvores SelecaoPorTorneio(List<Arvores> arvores) throws Exception {
        //Declaração Variáveis e Objetos
        ArrayList<Arvores> selecao = new ArrayList<>();

        //COPIAR 2 árvores Selecionadas Aleatóriamente
        selecao.add(arvores.get(mt.nextInt(arvores.size() - 1)));
        selecao.add(arvores.get(mt.nextInt(arvores.size() - 1)));

        //Retornar a melhor arvore (Clonar o Objeto - Cópia Profunda)
        return ((Arvores) (selecao.get(0).getFitness() < selecao.get(1).getFitness() ? selecao.get(0) : selecao.get(1)));

    }

    //Efetuar o crossover da população de árvores, aonde ocorre a Troca Genética de Material entre as árvores CRIANDO novas árvores
    private ArrayList<Arvores> CrossoverArvores(Arvores arv1, Arvores arv2) {
        //Declaração Variáveis e Objetos e Inicializações
        ArrayList<Arvores> populacao = new ArrayList<>();

        try {
            //Declaração Variáveis e Objetos
            //Deverá ser avaliado a opção de que as 2 Arvores devm possuir nodos ALÉM do nodo raiz, se possuir processa caso contrário não
            String atributoArv1 = BuscarAtributosArvore(arv1);
            String atributoArv2 = BuscarAtributosArvore(arv2);

            //Se o atributo for válido processa SENÃO abandona o processamento
            if (!atributoArv1.isEmpty()) {
                //Buscar um dos Atributos Selecionados Aleatóriamente e Remove a Sub-Árvore da 1° Árvore Transformando em um Nodo "Folha" na posição 
                RemoverNodoNaOrigemSetandoNuloERetornandoArvore(arv1, atributoArv1, 0);

                //Incluir o nodo removido na 1° Árvore em uma posição aleatório da 2° Árvore (desde que a posição seja um nodo folha)
                PesquisarPosicaoArvoreDestino(arv2);

            }

            //Se o atributo for válido processa SENÃO abandona o processamento
            if (!atributoArv2.isEmpty()) {
                //Buscar um dos atributos selecionados aleatóriamente e Remover Sub-Árvore da 2° Árvore transformando em um nodo folha na respectiva posição 
                RemoverNodoNaOrigemSetandoNuloERetornandoArvore(arv2, atributoArv2, 0);

                //Incluir o nodo removido na 1° Árvore em uma posição aleatória da 2° Árvore (desde que a posição seja um nodo folha)
                PesquisarPosicaoArvoreDestino(arv1);

            }

            //Adicionar as árvores c/ o "CROSSOVER" efetuado e limpar o objeto
            populacao.add(arv1);
            populacao.add(arv2);

            //Liberar o Objeto
            arvTemporaria = null;

        } catch (Exception e) {
            System.out.println(e.getMessage());

        }

        //Definir o retorno
        return populacao;

    }
    //</editor-fold>        

    //<editor-fold defaultstate="collapsed" desc="Funções Pertinentes aos Métodos de Mutação">   
    //Efetuar a Mutação da árvore, a mesma poderá ser de "E"xpansão ou "R"edução
    private void MutacaoArvores(Arvores arvore, String tipo, Instances dados, String atributo, int posicao) throws IOException {
        /*
         SE for "E" - EXPANSÃO - Vai até um nodo FOLHA ALEATÓRIO E ADICIONA um AlGEnArDe Aleatóriamente
         SENÃO  "R" - REDUÇÃO  - Vai até o nodo passado como parâmetro e transforma-se todos as sub-árvores abaixo em folhas
         */
        //Se possuir arestas válidas
        if (arvore.getArestas() != null) {
            //Se for "EXPANSÃO"
            if (tipo.equals("E")) {
                //Declaração Variáveis e Objetos - Selecionar uma posição aleatória
                int itemPos = arvore.getArestas().size() <= 1 ? 0 : mt.nextInt(arvore.getArestas().size() - 1);
                ArrayList<Arvores> temp = (ArrayList<Arvores>) ObjectUtil.deepCopyList(AlGEnArDe.nodos);

                //Se a aresta selecionada não for nula percorre até encontrar uma aresta nula
                if (arvore.getArestas(itemPos) != null) {
                    //Se o nodo da aresta não for nulo
                    if (arvore.getArestas(itemPos).getNodo() != null) {
                        //Se atingiu o MAIOR NÍVEL de profundidade da árvore (O último nodo da aresta DEVERÁ SER nulo) - Avaliando a aresta SELECIONADA aleatóriamente
                        if (arvore.getArestas(itemPos).getNodo().getArestas(itemPos).getNodo() == null) {
                            //Selecionar aleatóriamente uma árvore p/ ser incluida no nodo raiz e Adicionar o nodo na aresta selecionada
                            arvore.getArestas(itemPos).getNodo().getArestas(itemPos).setNodo(temp.get(Processamento.mt.nextInt(temp.size() - 1)));

                        } else {
                            //Chamar a função recursivamente até chegar em um nodo raiz
                            MutacaoArvores(arvore.getArestas(itemPos).getNodo(), tipo, dados, atributo, posicao);

                        }

                    } else {
                        //Se o nodo for nulo, seleciona uma Decision Stump aleatóriamente p/ ser incluida no nodo folha
                        arvore.getArestas(itemPos).setNodo(temp.get(Processamento.mt.nextInt(temp.size() - 1)));

                    }

                } else //Se a aresta for nula insere 
                {
                    //Declaraçãio variáveis e Objetos
                    Arvores arvTempor = temp.get(Processamento.mt.nextInt(temp.size() - 1));
                    ArrayList<Atributos> atribs = new ArrayList<>();

                    //Atribuições
                    atribs.add(new Atributos(arvTempor.getNomeAtributo(), null, "", null));

                    //Se o nodo for nulo, seleciona uma Decision Stump aleatóriamente p/ ser incluida no nodo folha                    
                    arvore.setArestas(atribs);

                }

            } else {
                //Se for "REDUÇÃO"
                //Se o nodo não for nulo
                if (arvore.getArestas(posicao).getNodo() != null) {
                    //Se for o Atributo Selecionado Atribuo nulo senão retorno pra pesquisa
                    if (arvore.getArestas(posicao).getNodo().getNomeAtributo().equals(atributo)) {
                        //Transformar o Nodo c/ arestas em Nodo Folha (Mutação de "REDUÇÃO") E Sair do processamento
                        arvore.getArestas(posicao).setNodo(null);
                        //Sair fora da execução -- NAO REMOVER
                        return;

                    } else {
                        //Chamada recursiva da função atualizando o nível de profundidade
                        MutacaoArvores(arvore.getArestas(posicao).getNodo(), tipo, dados, atributo, posicao);

                    }

                } else {
                    //Chamada recursiva da função atualizando o nível de profundidade
                    MutacaoArvores(arvore.getArestas(posicao + 1).getNodo(), tipo, dados, atributo, posicao + 1);

                }

            }

        }

    }
    //</editor-fold>        

    //<editor-fold defaultstate="collapsed" desc="Funções Destinadas ao Crossover">
    //Remover uma Sub-Árvore da Árvore atual e setar nulo a mesma
    public void RemoverNodoNaOrigemSetandoNuloERetornandoArvore(Arvores arvore, String atributo, int posicao) {
        try {
            if (arvore != null) {
                //Se as arestas não forem nulas
                if (arvore.getArestas() != null) {
                    //Se a aresta selecionada não for nula
                    if (arvore.getArestas(posicao) != null) {
                        //Se o nodo não for nulo
                        if (arvore.getArestas(posicao).getNodo() != null) {
                            //Se for o nodo da aresta selecionado aleatóriamente
                            if (arvore.getArestas(posicao).getNodo().getNomeAtributo().equals(atributo)) {
                                //Criar o Objeto e Inicializar o mesmo
                                arvTemporaria = new Arvores();
                                arvTemporaria = arvore.getArestas(posicao).getNodo();

                                //Setar nulo p/ a sub-árvore selecionada
                                arvore.getArestas(posicao).setNodo(null);
                                arvore.getArestas(posicao).setClasses(null);
                                arvore.getArestas(posicao).setClasseDominante("");

                                //Sair fora
                                return;

                            }
                            //Chamar Recursivamente a função até encontrar o nodo
                            RemoverNodoNaOrigemSetandoNuloERetornandoArvore(arvore.getArestas(posicao).getNodo(), atributo, posicao);

                        }

                    } else {
                        //Chamar Recursivamente a função até encontrar o nodo
                        RemoverNodoNaOrigemSetandoNuloERetornandoArvore(arvore.getArestas(posicao + 1).getNodo(), atributo, posicao + 1);

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
            //Se possuir arestas
            if (arvore.getArestas() != null) {
                //Selecionar uma posição aleatóriamente (Não importa qual, pois é inclusão de um novo nodo)
                int posicao = arvore.getArestas().size() <= 1 ? 0 : mt.nextInt(arvore.getArestas().size() - 1);

                //Se o Nodo da Aresta não for nulo, existem Sub-Árvores
                if (arvore.getArestas(posicao).getNodo() != null) {
                    //Chamada Recursiva da Função p/ Avaliação da Sub-Árvore informada
                    PesquisarPosicaoArvoreDestino(arvore.getArestas(posicao).getNodo());

                } else {
                    //Se a aresta informada for nula insere o nodo e finaliza o ciclo
                    arvore.getArestas(posicao).setNodo(arvTemporaria);

                    //Setar as propriedades
                    arvore.getArestas(posicao).setClasses(null);
                    arvore.getArestas(posicao).setClasseDominante("");

                }

            }

        } catch (Exception e) {
            System.out.println(e.getMessage());

        }

    }

    //Atribui ao nodo folha a(s) classe(s) a qual pertence, se não existe atribui a mesma senão atualiza a quantidade em um unidade(caso exista a mesma)
    public void AtribuicaoClasseNodosFolhas(Arvores arvore, Instance avaliacao) {
        //Declaração Variáveis e objetos
        int posicao = 0;

        //Se o árvore não for nula
        if (arvore != null) {
            //Percorrer todos os atributos da instância selecionada (Exceto o atributo Classe)
            for (int k = 0; k < avaliacao.numAttributes(); k++) {
                //Se o nome do Atributo Classe for igual ao nome do atributo da instância (Raiz ou nodo folha) - DEVIDO A NÃO TER ACESSO PELO NOME DO ATRIBUTO
                if (avaliacao.attribute(k).name().equals(arvore.getNomeAtributo())) {
                    //Atribuições
                    posicao = k;

                    //Sair fora do for
                    break;

                }

            }

            //Se o atributo for Numérico (BIFURCAÇÃO)
            if (avaliacao.attribute(posicao).isNumeric()) {
                //Se a aresta não for nula
                if (arvore.getArestas() != null) {
                    //Declaração Variáveis e Objetos
                    double valorAresta = Double.valueOf(arvore.getArestas(0).getAtributo().split(" ")[1]);

                    //Se o valor da posição FOR MENOR OU IGUAL ao valor do atributo selecionado (Então posição igual a 0 SENAO 1)
                    int itemPos = Arredondar(avaliacao.value(posicao), AlGEnArDe.qtdDecimais, 1) <= Arredondar(valorAresta, AlGEnArDe.qtdDecimais, 1) ? 0 : 1;

                    //Se não for um nodo RAIZ, efetua a chamada recursiva da função até chegar em um nodo raiz
                    if (arvore.getArestas(itemPos).getNodo() != null) {
                        //Chama a função recursivamente passando o nodo da aresta
                        AtribuicaoClasseNodosFolhas(arvore.getArestas(itemPos).getNodo(), avaliacao);

                    } else {
                        //Declaração Variáveis e Objetos
                        ArrayList<Classes> classes = new ArrayList<>();

                        //Se a Classe for vazia Inclui o mesmo (Sendo o 1° Registro)
                        if (arvore.getArestas(itemPos).getClasses() == null) {
                            //Adicionar a Nova classe 
                            classes.add(new Classes(avaliacao.classAttribute().value((int) avaliacao.classValue()), 1));

                            //Atribuir as classes e sair fora da execução para a aresta selecionada
                            arvore.getArestas(itemPos).setClasses(classes);

                        } else //Já Existem Registros na Classe, irá atualizar o mesmo
                        {
                            //Declaração Variáveis e Objetos
                            boolean processar = false;
                            classes = arvore.getArestas(itemPos).getClasses();

                            //Percorre TODAS as classes do Nodo
                            for (Classes classe : classes) {
                                //Se o valor da aresta FOR IGUAL AO VALOR DO ATRIBUTO DA INSTÂNCIA                                            
                                //Se o "VALOR" da classe DA INSTÂNCIA SELECIONADA FOR IGUAL a da classe informada atualiza a quantidade
                                if (avaliacao.classAttribute().value((int) avaliacao.classValue()).equals(classe.getNome())) {
                                    //Atualizar a quantidade (Adicionando 1) de registros X Atributo - Para Definir a Classe dominante
                                    classe.atualizarQtd(1);
                                    processar = true;

                                }

                            }

                            //Se não existe a CLASSE avaliada insere a mesma
                            if (!processar) {
                                //Se não for nulo (ser o último)
                                if (avaliacao.classAttribute() != null) {
                                    //Adicionar a Nova classe e atualizar a quantidade
                                    classes.add(new Classes(avaliacao.classAttribute().value((int) avaliacao.classValue()), 1));

                                }

                            }

                            //Atribuir as classes e sair fora da execução para a aresta selecionada
                            arvore.getArestas(itemPos).setClasses(classes);

                        }

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
                                if (arvore.getArestas(i).getAtributo().equals(avaliacao.attribute(posicao).value(l))) {
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

        }

    }

    //Irá percorrer todos os Nodos da árvore(avaliando SOMENTE os nodos folhas) 
    public void DefinicaoClasseMajoritariaNodosFolhas(Arvores arvore, int posicao) {
        //Se a aresta selecionada não for NULA pesquisa pela mesma (NULA == Nodo Folha)
        if (arvore != null) {
            if (arvore.getArestas(posicao) != null) {
                //Atualizar a Classe Dominante da Aresta
                atualizarClasseDominante(arvore, posicao);

                //Chamada recursiva da função passando como parâmetros a aresta selecionada
                DefinicaoClasseMajoritariaNodosFolhas(arvore.getArestas(posicao).getNodo(), posicao);
                DefinicaoClasseMajoritariaNodosFolhas(arvore.getArestas(posicao + 1).getNodo(), posicao + 1);

            }

        }

    }
    //</editor-fold>        

    //<editor-fold defaultstate="collapsed" desc="Calcular o Valor Médio da Árvores(arestas) - Para Atributos Numéricos">    
    //Efetuar o Cálculo do Indice Gini p/ Atributos Contínuos 
    //Por Exemplo Indice Gini = 1 - (Somatório Quant. Atrib. "A" / Total de Instâncias) ^ 2 - (Somatório Quant. Atrib. "N" / Total de Instâncias) ^ 2.
    private double calcularIndiceGini(Instances dados, int pos) {
        //Declaração Variáveis e objetos
        List<IndiceGini> filtrados, valores = new ArrayList<>();
        List<Double> indice = new ArrayList<>();
        ArrayList<Classes> regs;

        //Criar o filtro lógico
        Filter<IndiceGini, Double> filtro = new Filter<IndiceGini, Double>() {
            @Override
            public boolean isMatched(IndiceGini object, Double valor) {
                return object.getValor() == valor;

            }
        };

        //Pegar distintamente os valores p/ calcular a média de todas as instâncias do atributo informado, adicionar os valores das instâncias na posição informada
        for (int i = 0; i < dados.numInstances(); i++) {
            //Filtragem das ocorrências pelo Atributo "Valor"
            filtrados = new FilterList().filterList(valores, filtro, dados.instance(i).value(pos));
            String clsAtributo = dados.instance(i).classAttribute().value((int) dados.instance(i).classValue());

            //Se não encontrou registros, inclui o mesmo
            if (filtrados.isEmpty()) {
                //Criação do Objeto
                regs = new ArrayList<>();

                //Adicionar as propriedades
                regs.add(new Classes(clsAtributo, 1));

                //Incluir o Valor
                valores.add(new IndiceGini(dados.instance(i).value(pos), regs));

            } else {
                //Declaração Variáveis e Objetos
                boolean bOk = false;

                for (IndiceGini sel : filtrados) {
                    //Somente poderá encontrar um registro e ai percorre as suas classes
                    for (Classes atr : sel.getClsAtribruto()) {
                        //Se o nome do atributo atributo for igual
                        if (atr.getNome().equals(clsAtributo)) {
                            //Atualizar a quantidade, e sair fora da pesquisa
                            atr.atualizarQtd(1);
                            bOk = true;
                            break;

                        }

                    }

                    //Se não for nenhum deles
                    if (!bOk) {
                        //Adicionar as propriedades
                        sel.getClsAtribruto().add(new Classes(dados.instance(i).classAttribute().value((int) dados.instance(i).classValue()), 1));

                    }

                }

            }

        }

        //Ordenar Crescente dos atributos
        Collections.sort(valores);

        //Percorrer os valores cadastrados
        for (IndiceGini item : valores) {
            //Declaração Variáveis e objetos
            int total = 0;
            double indGini = 1d;

            if (item.getClsAtribruto() != null) {
                //Percorrer todos os itens
                for (Classes clsItem : item.getClsAtribruto()) {
                    //Totalizar a quantidade
                    total += clsItem.getQuantidade();

                }

                //Percorrer todos os itens
                for (Classes cls : item.getClsAtribruto()) {
                    //Calcular o Indice Gini
                    indGini -= Math.pow(((double) cls.getQuantidade() / total), 2);

                }

            }

            //Adicionar o Indice Gini Calculado
            indice.add(Arredondar(indGini, AlGEnArDe.qtdDecimais, 1));

        }

        //Ordenar os indices em ordem crescente
        Collections.sort(indice);

        //Definir o Retorno (se o Índice Gini for "exatamente" 1 deverá ser 0)
        return indice.get(0) <= 1 ? 0 : indice.get(0);

    }

    // Parâmetros: 1 - Valor a Arredondar. 
    //             2 - Quantidade de casas depois da vírgula. 
    //             3 - Arredondar para cima ou para baixo?
    // Para Cima  = 0 (ceil) 
    // Para Baixo = 1 ou qualquer outro inteiro (floor)
    public double Arredondar(double valor, int casas, int ACimaouABaixo) {
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
        //Declaração Variáveis e Objetos e Inicializações
        String nomeatributo = "";
        ArrayList<String> atributos = new ArrayList<>();

        try {
            //Chamada Recursiva p/ montagem dos atributos da árvore(apartir do nodo raiz)
            processarNomesAtributos(arv, 0, atributos);

            //Se for diferente de nulo(carregou algum atribuito)
            if (atributos.size() > 0) {
                //Não poderá retornar o nodo raiz, por isto a exclusão do nodo na posição 0, sendo assim remove-se o nodo raiz
                atributos.remove(0);

                //Se possuir Árvores 
                if (atributos.size() > 0) {
                    //Se o tamanho for 1, pega o único existente (sem sorteio) SENÃO Sortear um entre os possíveis
                    nomeatributo = (atributos.size() <= 1) ? atributos.get(0) : atributos.get(mt.nextInt(atributos.size() - 1));

                }

            }

        } catch (Exception e) {
            return "Erro Atributos.: " + e.getMessage();

        }

        //Definir o retorno
        return nomeatributo;

    }

    //Localizar os atributos(nomes) existentes na árvore, aonde será sorteado um deles para mutação (EXCETO o nodo Raiz)
    private void processarNomesAtributos(Arvores arv, int posicao, ArrayList<String> atributos) {
        //Se a árvore não for nula
        if (arv != null) {
            //Se possuir arestas válidas
            if (arv.getArestas() != null) {
                //Se a aresta informada for válida
                if (arv.getArestas(posicao) != null) {
                    //Se for vazio Adiciona senão avalia e depois insere sim ou não
                    if (atributos.isEmpty()) {
                        //Adicionar o nome do Atributo SE não Contiver
                        atributos.add(arv.getNomeAtributo());

                    } else {
                        if (!atributos.contains(arv.getNomeAtributo())) {
                            //Adicionar o Atributo
                            atributos.add(arv.getNomeAtributo());

                        }

                    }

                    //Se o nodo não for nulo
                    if (arv.getArestas(posicao) != null) {
                        //Chamada recursiva da árvore passando o nodo selecionado
                        processarNomesAtributos(arv.getArestas(posicao).getNodo(), posicao, atributos);

                        //Se o nodo da próxima posição NÃO FOR NULO, processa SENÃO retorna a quem chamou
                        if (arv.getArestas(posicao + 1) != null) {
                            //Chamada recursiva da árvore passando o nodo selecionado posição +1 
                            processarNomesAtributos(arv.getArestas(posicao + 1).getNodo(), posicao + 1, atributos);

                        }

                    } else {
                        //Se o nodo da próxima posição NÃO FOR NULO, processa SENÃO retorna a quem chamou
                        if (arv.getArestas(posicao + 1) != null) {
                            //Chamada recursiva da árvore passando o nodo selecionado posição +1 
                            processarNomesAtributos(arv.getArestas(posicao + 1).getNodo(), posicao + 1, atributos);

                        }

                    }

                } else {
                    //Se o nodo da próxima posição NÃO FOR NULO, processa SENÃO retorna a quem chamou
                    if (arv.getArestas(posicao + 1) != null) {
                        //Chamada recursiva da árvore passando o nodo selecionado posição +1 
                        processarNomesAtributos(arv.getArestas(posicao + 1).getNodo(), posicao + 1, atributos);

                    }

                }

            }

        }

    }

//</editor-fold>        
    private void atualizarClasseDominante(Arvores arvore, int posicao) {
        //Se possuir classes
        if (arvore.getArestas(posicao).getClasses() != null) {
            //Declaração Variáveis e Objetos
            ArrayList<Classes> classes = arvore.getArestas(posicao).getClasses();

            //Se não for nulo
            if (classes != null) {
                //Declaração Variáveis e Objetos
                String clsDominante = "";
                double qtdOcorrCls = 0;

                //Percorre todas as Classes
                for (Classes classe : classes) {
                    //Se for a 1° Ocorrência
                    if (clsDominante.isEmpty()) {
                        //Atribuições do nome da classe e da quantidade
                        clsDominante = classe.getNome();
                        qtdOcorrCls = classe.getQuantidade();

                    } else {
                        //Se a quantidade for MAIOR que a ATUAL ALTERA a classe SENÃO mantém a mesma
                        if (classe.getQuantidade() > qtdOcorrCls) {
                            //Atribuições do nome da classe e da quantidade
                            clsDominante = classe.getNome();
                            qtdOcorrCls = classe.getQuantidade();

                        }

                    }

                }

                //Setar a classe Majoritária
                arvore.getArestas(posicao).setClasseDominante(clsDominante);

            }

        }
    }
}
