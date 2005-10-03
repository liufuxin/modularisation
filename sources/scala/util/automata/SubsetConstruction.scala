package scala.util.automata ;

class SubsetConstruction[T <: AnyRef](val nfa: NondetWordAutom[T]) {

  //import nfa.{ _labelT, labels };
  import nfa.labels ;
  import scala.collection.{immutable, mutable, Map} ;

  import immutable.{ BitSet, TreeMap, TreeSet } ;

  /** the set {0} */
  final val _initialBitSet = {
    val rbs = new mutable.BitSet(1);
    rbs.set(0);
    rbs.makeImmutable;
  }

  /** the set {} */
  final val _sinkBitSet = {
    new mutable.BitSet(1).makeImmutable;
  }

  final val _emptyBitSet =  {
    val rbs = new scala.collection.mutable.BitSet(1);
    new BitSet(rbs);
  }

  def selectTag(Q:BitSet, finals:Array[Int]) = {
    val it = Q.toSet(true).elements;
    var mintag = scala.runtime.compat.Math.MAX_INT;
    while(it.hasNext) {
      val tag = finals(it.next);
      if((0 < tag) && (tag < mintag))
        mintag = tag
    }
    mintag
  }
  
  def determinize: DetWordAutom[ T ] = {
    
    // for assigning numbers to bitsets
    var indexMap    = new TreeMap[ BitSet, Int ];
    var invIndexMap = new TreeMap[ Int, BitSet ];
    var ix = 0;

    // we compute the dfa with states = bitsets
    var states   = new TreeSet[BitSet]();
    val delta    = new mutable.HashMap[BitSet,
                                       mutable.HashMap[T, BitSet]];
    var deftrans = new TreeMap[BitSet, BitSet];
    var finals   = new TreeMap[BitSet, Int];

    val q0 = _initialBitSet;
    states = states + q0;
    
    val sink = _emptyBitSet;
    states = states + sink;

    deftrans = deftrans.update(q0,sink);
    deftrans = deftrans.update(sink,sink);

    val rest = new mutable.Stack[BitSet]();

    def add(Q: BitSet): Unit = {
      if(!states.contains(Q)) {
        states = states + Q;
        rest.push(Q);
          if(nfa.containsFinal(Q))
            finals = finals.update(Q, selectTag(Q,nfa.finals));
      }
   } 
    rest.push( sink ); 
    val sinkIndex = 1;
    rest.push( q0 );
    while(!rest.isEmpty) {
      // assign a number to this bitset
      val P = rest.pop;
      indexMap = indexMap.update(P,ix);
      invIndexMap = invIndexMap.update(ix,P);
      ix = ix + 1;

      // make transitiion map
      val Pdelta = new mutable.HashMap[T, BitSet];
      delta.update( P, Pdelta );

      val it = labels.elements; while(it.hasNext) {
        val label = it.next;

        val Q = nfa.next(P,label);

	Pdelta.update( label, Q );
        
        add(Q);
      }

      // collect default transitions
      val Pdef = nfa.nextDefault(P);
      deftrans = deftrans.update(P,Pdef);
      add(Pdef);
    };

    // create DetWordAutom, using indices instead of sets
    val nstatesR = states.size;
    val deltaR = new Array[Map[T,Int]](nstatesR);
    val defaultR = new Array[Int](nstatesR);
    val finalsR = new Array[Int](nstatesR);
  
    for(val w <- states) {
      val Q = w;
      val q = indexMap(Q);
      val trans = delta(Q);
      val transDef = deftrans(Q);
      val qDef = indexMap(transDef);
      val ntrans = new mutable.HashMap[T,Int]();
      val it = trans.keys; while(it.hasNext) {
        val label = it.next;
        val p = indexMap(trans(label));
        if( p != qDef )
          ntrans.update(label, p)
      }
      deltaR.update(q, ntrans);
      defaultR.update(q, qDef);

      //cleanup? leave to garbage collector?
      //delta.remove(Q);
      //default.remove(Q);
      
    }

    for(val fQ <- finals.keys) {
      finalsR(indexMap(fQ)) = finals(fQ);
    }

    new DetWordAutom [ T ] {
      
      //type _labelT = SubsetConstruction.this.nfa._labelT;
      val nstates = nstatesR;
      val delta = deltaR;
      val default = defaultR;
      val finals = finalsR;
    }
  }
}
