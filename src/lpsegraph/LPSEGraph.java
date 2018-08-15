/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lpsegraph;

/**
 *
 * @author COMPAQ
 */
public class LPSEGraph {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        
        CSVProcess csv_process = new CSVProcess();
        csv_process.script(args[0], args[1], Integer.decode(args[2]), Integer.decode(args[3]), Double.parseDouble(args[4]), Double.parseDouble(args[5]), Double.parseDouble(args[6]), Double.parseDouble(args[7]), Integer.parseInt(args[8]), Double.parseDouble(args[9]));
        csv_process.script("E:\\PROJECT_PKL\\LPSE-UGM-Winner-Parser\\bin\\Debug\\Resources\\cache\\LPSE Kabupaten Bantul.csv", "E:\\PROJECT_PKL\\test.pdf", 0xFFC0D9, 0xAAAAFF, 0.8, 3, 0.6, 3, 120, 2000);
        //source0, destination1, colorMin2, colorMax3, rankSizeMin4, rankSizeMax5, labelSizeMin6, labelSizeMax7, seconds8, repulsion9
    }
    
}
