package edu.cmu.graphchi.apps;

import edu.cmu.graphchi.*;
import edu.cmu.graphchi.datablocks.FloatConverter;
import edu.cmu.graphchi.engine.HypergraphChiEngine;
import edu.cmu.graphchi.engine.VertexInterval;
import edu.cmu.graphchi.io.CompressedIO;
import edu.cmu.graphchi.preprocessing.EdgeProcessor;
import edu.cmu.graphchi.preprocessing.FastSharder;
import edu.cmu.graphchi.preprocessing.VertexIdTranslate;
import edu.cmu.graphchi.preprocessing.VertexProcessor;
import edu.cmu.graphchi.util.IdFloat;
import edu.cmu.graphchi.util.Toplist;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.TreeSet;
import java.util.logging.Logger;

// 
/**
 * Example application: PageRank (http://en.wikipedia.org/wiki/Pagerank)
 * Iteratively computes a pagerank for each vertex by averaging the pageranks
 * of in-neighbors pageranks.
 * 
 */
public class HypergraphPagerank implements HypergraphChiProgram<Float, Float> {

    private static Logger logger = ChiLogger.getLogger("pagerank");
    
    public void updateHyperedge(ChiVertex<Float, Float> vertex, GraphChiContext context)  {
    	//System.out.println("\n ------------- start Hyperedge ---------------\n");
        if (context.getIteration() == 0) {
            /* Initialize on first iteration */
            vertex.setValue(0.0f);
        } else {
            /* On other iterations, set my value to be the weighted
               average of my in-coming neighbors pageranks.
             */
            float sum = 0.f;
            for(int i=0; i<vertex.numInEdges(); i++) {
                sum += vertex.inEdge(i).getValue();
          //      System.out.println("\ni "+vertex.inEdge(i).getVertexId()+" inEdge value : "+ vertex.inEdge(i).getValue());
                
            }
            vertex.setValue(0.2f + 0.8f * sum);
            
            /*System.out.println("\n----Iteration: "+context.getIteration()+
            		" Vertex ID: "+vertex.getId()+
            		" Value : " +vertex.getValue()+"\n"+
            		" Sum : " +sum+"\n");*/
        }

        /* Write my value (divided by my out-degree) to my out-edges so neighbors can read it. */
        
        float outValue = vertex.getValue() / vertex.numOutEdges();
        
       /* 
        System.out.println("\n----Iteration: "+context.getIteration()+
        		" Out value: "+ outValue+"\t"+
        		" Vertex ID: "+vertex.getId()+
        		" Vertex Value: "+vertex.getValue()+
        		" out egdes : " +vertex.numOutEdges()+"\t"+ 
        		" in egdes : " +vertex.numInEdges()+"\n");
        
        */
        for(int i=0; i<vertex.numOutEdges(); i++) {
            vertex.outEdge(i).setValue(outValue);
        }
        //System.out.println("\n --------------end Hyperedge-------\n");
    }

    public void updateVertex(ChiVertex<Float, Float> vertex, GraphChiContext context)  {
    	
    //	System.out.println("\n --------------vertex-------\n");
        
    	if (context.getIteration() == 0) {
            /* Initialize on first iteration */
            vertex.setValue(1.0f);
        } else {
            /* On other iterations, set my value to be the weighted
               average of my in-coming neighbors pageranks.
             */
            float sum = 0.f;
        
            for(int i=0; i<vertex.numInEdges(); i++) {
                sum += vertex.inEdge(i).getValue();
                System.out.println("\ni "+vertex.inEdge(i).getVertexId()+" inEdge value : "+ vertex.inEdge(i).getValue());
            }
            
            vertex.setValue(0.2f + 0.80f * sum);
        
      /*      System.out.println("\n----Iteration: "+context.getIteration()+
            		" Vertex ID: "+vertex.getId()+
            		" Value : " +vertex.getValue()+"\n"+
            		" Sum : " +sum+"\n");*/
        }

        /* Write my value (divided by my out-degree) to my out-edges so neighbors can read it. */
        float outValue = vertex.getValue() / vertex.numOutEdges();
        
     /*   System.out.println("\n----Iteration: "+context.getIteration()+
        		" Out value: "+ outValue+"\t"+
        		" Vertex ID: "+vertex.getId()+
        		" Vertex Value: "+vertex.getValue()+
        		" out egdes : " +vertex.numOutEdges()+"\t"+ 
        		" in egdes : " +vertex.numInEdges()+"\n");*/
        
        for(int i=0; i<vertex.numOutEdges(); i++) {
            vertex.outEdge(i).setValue(outValue);
        }
        
        //System.out.println("\n --------------vertex-------\n");
    }

    public void update(ChiVertex<Float, Float> vertex, GraphChiContext context)  {
        if (context.getIteration() == 0) {
            /* Initialize on first iteration */
            vertex.setValue(1.0f);
        } else {
            /* On other iterations, set my value to be the weighted
               average of my in-coming neighbors pageranks.
             */
            float sum = 0.f;
            for(int i=0; i<vertex.numInEdges(); i++) {
                sum += vertex.inEdge(i).getValue();
            }
            vertex.setValue(0.15f + 0.85f * sum);
        }

        /* Write my value (divided by my out-degree) to my out-edges so neighbors can read it. */
        float outValue = vertex.getValue() / vertex.numOutEdges();
        for(int i=0; i<vertex.numOutEdges(); i++) {
            vertex.outEdge(i).setValue(outValue);
        }

    }

    /**
     * Callbacks (not needed for Pagerank)
     */
    public void beginIteration(GraphChiContext ctx) {}
    public void endIteration(GraphChiContext ctx) {}
    public void beginInterval(GraphChiContext ctx, VertexInterval interval) {}
    public void endInterval(GraphChiContext ctx, VertexInterval interval) {}
    public void beginSubInterval(GraphChiContext ctx, VertexInterval interval) {}
    public void endSubInterval(GraphChiContext ctx, VertexInterval interval) {}

    /** Hypergraph
     * Initialize the sharder-program
     * @param graphName
     * @param numShards
     * @return
     * @throws IOException
     */
    protected static FastSharder createSharder(String graphName, int numShards) throws IOException {
        return new FastSharder<Float, Float>(graphName, numShards, new VertexProcessor<Float>() {
            public Float receiveVertexValue(int vertexId, String token) {
                return (token == null ? 0.0f : Float.parseFloat(token));
            }
        }, new EdgeProcessor<Float>() {
            public Float receiveEdge(int from, int to, String token) {
                return (token == null ? 0.0f : Float.parseFloat(token));
            }
        }, new FloatConverter(), new FloatConverter());
    }

    
    /**
     * Usage: java edu.cmu.graphchi.demo.PageRank graph-name num-shards filetype(edgelist|adjlist)
     * For specifying the number of shards, 20-50 million edges/shard is often a good configuration.
     */
    public static void main(String[] args) throws  Exception {
        String baseFilename = args[0];
        int nShards = Integer.parseInt(args[1]);
        String fileType = (args.length >= 3 ? args[2] : null);

        CompressedIO.disableCompression();

        /* Create shards */
        FastSharder sharder = createSharder(baseFilename, nShards);
        if (baseFilename.equals("pipein")) {     // Allow piping graph in
            sharder.shard(System.in, fileType);
        } else {
            if (!new File(ChiFilenames.getFilenameIntervals(baseFilename, nShards)).exists()) {
                sharder.shard(new FileInputStream(new File(baseFilename)), fileType);
            } else {
                logger.info("Found shards -- no need to preprocess");
            }
        }

        /* Run GraphChi */
        HypergraphChiEngine<Float, Float> engine = new HypergraphChiEngine<Float, Float>(baseFilename, nShards);
        engine.setEdataConverter(new FloatConverter());
        engine.setVertexDataConverter(new FloatConverter());
        engine.setModifiesInedges(false); // Important optimization

        // after initializing hyperedges, call this
        engine.run(new HypergraphPagerank(), 8);

        logger.info("Ready.");

        /* Output results */
        int i = 0;
        VertexIdTranslate trans = engine.getVertexIdTranslate();
        
        TreeSet<IdFloat> top20 = Toplist.topListFloat(baseFilename, engine.numVertices(), 20);
        for(IdFloat vertexRank : top20) {
   //     	System.out.println(++i + ": " + trans.backward(vertexRank.getVertexId()) + " = " + vertexRank.getVertexId());
        	System.out.println(++i + ": " + trans.backward(vertexRank.getVertexId()) + " = " + vertexRank.getValue());
        }
    }
}
