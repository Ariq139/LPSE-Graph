/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lpsegraph;

import java.awt.Color;
import java.io.File;
import java.lang.Object;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.gephi.appearance.api.AppearanceController;
import org.gephi.appearance.api.AppearanceModel;
import org.gephi.appearance.api.Function;
import org.gephi.appearance.plugin.RankingElementColorTransformer;
import org.gephi.appearance.plugin.RankingLabelSizeTransformer;
import org.gephi.appearance.plugin.RankingNodeSizeTransformer;
import org.gephi.filters.api.FilterController;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.EdgeDirectionDefault;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.importer.plugin.file.spreadsheet.ImporterSpreadsheetCSV;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.layout.plugin.AutoLayout;
import org.gephi.layout.plugin.force.StepDisplacement;
import org.gephi.layout.plugin.force.yifanHu.YifanHuLayout;
import org.gephi.layout.plugin.forceAtlas.ForceAtlasLayout;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.statistics.plugin.GraphDistance;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
/**
 *
 * @author COMPAQ
 */
public class CSVProcess {
    public void script(String source, String destination, int colorMin, int colorMax, double rankSizeMin, double rankSizeMax, double labelSizeMin, double labelSizeMax, int seconds, double repulsion){
         
        //Init a project - and therefore a workspace
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        Workspace workspace = pc.getCurrentWorkspace();
        
        //Get models and controllers for this new workspace - will be useful later
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        PreviewModel model = Lookup.getDefault().lookup(PreviewController.class).getModel();
        ImportController importController = Lookup.getDefault().lookup(ImportController.class);
        ImporterSpreadsheetCSV csv_importer = Lookup.getDefault().lookup(ImporterSpreadsheetCSV.class);
        FilterController filterController = Lookup.getDefault().lookup(FilterController.class);
        AppearanceController appearanceController = Lookup.getDefault().lookup(AppearanceController.class);
        AppearanceModel appearanceModel = appearanceController.getModel();
        
        Container container;
        try {
            //File file = new File("E:\\PROJECT PKL\\ugm.csv"); //TODO: replace code for dynamic input
            File file = new File(source);
            container = importController.importFile(file);
            container.getLoader().setEdgeDefault(EdgeDirectionDefault.DIRECTED);   //Force DIRECTED
            container.getLoader().setAllowAutoNode(true);
            container.getLoader().setAutoScale(true);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
        
        importController.process(container, new DefaultProcessor(), workspace);
        
        DirectedGraph graph = graphModel.getDirectedGraph();
        
        //Rank color by Degree
        Function degreeRanking = appearanceModel.getNodeFunction(graph, AppearanceModel.GraphFunction.NODE_DEGREE, RankingElementColorTransformer.class);
        RankingElementColorTransformer degreeTransformer = (RankingElementColorTransformer) degreeRanking.getTransformer();
        degreeTransformer.setColors(new Color[]{new Color(colorMin), new Color(colorMax)});
        degreeTransformer.setColorPositions(new float[]{0f, 1f});
        appearanceController.transform(degreeRanking);

        //Get Centrality
        GraphDistance distance = new GraphDistance();
        distance.setDirected(true);
        distance.execute(graphModel);

        //Rank size by Degree
        Function sizeRanking = appearanceModel.getNodeFunction(graph, AppearanceModel.GraphFunction.NODE_DEGREE, RankingNodeSizeTransformer.class);
        RankingNodeSizeTransformer centralityTransformer = (RankingNodeSizeTransformer) sizeRanking.getTransformer();
        centralityTransformer.setMinSize((float) rankSizeMin);
        centralityTransformer.setMaxSize((float) rankSizeMax);
        appearanceController.transform(sizeRanking);

        //Rank label size - set a multiplier size
        Function centralityRanking2 = appearanceModel.getNodeFunction(graph, AppearanceModel.GraphFunction.NODE_DEGREE, RankingLabelSizeTransformer.class);
        RankingLabelSizeTransformer labelSizeTransformer = (RankingLabelSizeTransformer) centralityRanking2.getTransformer();
        labelSizeTransformer.setMinSize((float) labelSizeMin);
        labelSizeTransformer.setMaxSize((float) labelSizeMax);
        appearanceController.transform(centralityRanking2);

        //Set 'show labels' option in Preview - and disable node size influence on text size
        PreviewModel previewModel = Lookup.getDefault().lookup(PreviewController.class).getModel();
        previewModel.getProperties().putValue(PreviewProperty.SHOW_NODE_LABELS, Boolean.TRUE);
        previewModel.getProperties().putValue(PreviewProperty.NODE_LABEL_PROPORTIONAL_SIZE, Boolean.FALSE);
        
        //Layout for 100 secons
        AutoLayout autoLayout = new AutoLayout(seconds, TimeUnit.SECONDS);
        autoLayout.setGraphModel(graphModel);
        ForceAtlasLayout secondLayout = new ForceAtlasLayout(null);
        AutoLayout.DynamicProperty adjustBySizeProperty = AutoLayout.createDynamicProperty("forceAtlas.adjustSizes.name", Boolean.TRUE, 0.1f);//True after 10% of layout time
        AutoLayout.DynamicProperty repulsionProperty = AutoLayout.createDynamicProperty("forceAtlas.repulsionStrength.name", repulsion, 0f);//2000 for the complete period
        autoLayout.addLayout(secondLayout, 1f, new AutoLayout.DynamicProperty[]{adjustBySizeProperty, repulsionProperty});
        autoLayout.execute();

        //Export
        ExportController ec = Lookup.getDefault().lookup(ExportController.class);
        try {
            ec.exportFile(new File(destination));
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }
    }
}
