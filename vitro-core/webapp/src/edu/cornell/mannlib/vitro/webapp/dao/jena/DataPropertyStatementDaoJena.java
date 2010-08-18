/*
Copyright (c) 2010, Cornell University
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice,
      this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice,
      this list of conditions and the following disclaimer in the documentation
      and/or other materials provided with the distribution.
    * Neither the name of Cornell University nor the names of its contributors
      may be used to endorse or promote products derived from this software
      without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.iterator.ClosableIterator;
import com.hp.hpl.jena.vocabulary.RDF;

import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.jena.event.IndividualUpdateEvent;

public class DataPropertyStatementDaoJena extends JenaBaseDao implements DataPropertyStatementDao
{

    public DataPropertyStatementDaoJena(WebappDaoFactoryJena wadf) {
        super(wadf);
    }

    public void deleteDataPropertyStatement(DataPropertyStatement dataPropertyStmt) {
    	deleteDataPropertyStatement(dataPropertyStmt, getOntModelSelector().getABoxModel());
    }

    public void deleteDataPropertyStatement( DataPropertyStatement dataPropertyStatement, OntModel ontModel )
    {
    	try {
	    	ontModel.enterCriticalSection(Lock.WRITE);
	    	getOntModel().getBaseModel().notifyEvent(new IndividualUpdateEvent(getWebappDaoFactory().getUserURI(),true,dataPropertyStatement.getIndividualURI()));
	        com.hp.hpl.jena.ontology.Individual ind = ontModel.getIndividual(dataPropertyStatement.getIndividualURI());
	        Property prop = ontModel.getProperty(dataPropertyStatement.getDatapropURI());
	        Literal l = jenaLiteralFromDataPropertyStatement(dataPropertyStatement, ontModel);
	        if (ind != null && prop != null && l != null) {
	            ontModel.getBaseModel().remove(ind, prop, l);
	        }
    	} finally {
    		getOntModel().getBaseModel().notifyEvent(new IndividualUpdateEvent(getWebappDaoFactory().getUserURI(),false,dataPropertyStatement.getIndividualURI()));
    		ontModel.leaveCriticalSection();
    		
    	}
    }

    public Individual fillExistingDataPropertyStatementsForIndividual( Individual entity/*, boolean allowAnyNameSpace*/)
    {
        if( entity.getURI() == null )
        {
            return entity;
        }
        else
        {
            getOntModel().enterCriticalSection(Lock.READ);
            try {
                Resource ind = getOntModel().getResource(entity.getURI());
                List<DataPropertyStatement> edList = new ArrayList<DataPropertyStatement>();
                StmtIterator stmtIt = ind.listProperties();
                while( stmtIt.hasNext() )
                {
                    Statement st = (Statement)stmtIt.next();
                    boolean addToList = /*allowAnyNameSpace ? st.getObject().canAs(Literal.class) :*/ st.getObject().isLiteral() && 
                          (
                              (RDF.value.equals(st.getPredicate()) || VitroVocabulary.value.equals(st.getPredicate().getURI())) 
                              || !(NONUSER_NAMESPACES.contains(st.getPredicate().getNameSpace()))
                          );
                    if( addToList )
                    {   /* now want to expose Cornellemailnetid and potentially other properties so can at least control whether visible
                        boolean isExternalId = false;
                        ClosableIterator externalIdStmtIt = getOntModel().listStatements(st.getPredicate(), DATAPROPERTY_ISEXTERNALID, (Literal)null);
                        try {
                            if (externalIdStmtIt.hasNext()) {
                                isExternalId = true;
                            }
                        } finally {
                            externalIdStmtIt.close();
                        }
                        if (!isExternalId) { */
                        DataPropertyStatement ed = new DataPropertyStatementImpl();
                        Literal lit = (Literal)st.getObject();
                        fillDataPropertyStatementWithJenaLiteral(ed,lit);
                        ed.setDatapropURI(st.getPredicate().getURI());
                        ed.setIndividualURI(ind.getURI());
                        edList.add(ed);
                     /* } */
                    }
                }
                entity.setDataPropertyStatements(edList);
                return entity;
            } finally {
                getOntModel().leaveCriticalSection();
            }
        }
    }

    public void deleteDataPropertyStatementsForIndividualByDataProperty(String individualURI, String dataPropertyURI) {
        deleteDataPropertyStatementsForIndividualByDataProperty(individualURI, dataPropertyURI, getOntModelSelector().getABoxModel());
    }

    public void deleteDataPropertyStatementsForIndividualByDataProperty(String individualURI, String dataPropertyURI, OntModel ontModel) {
        ontModel.enterCriticalSection(Lock.WRITE);
        getOntModel().getBaseModel().notifyEvent(new IndividualUpdateEvent(getWebappDaoFactory().getUserURI(),true,individualURI));
        try {
            Resource indRes = ontModel.getResource(individualURI);
            DatatypeProperty datatypeProperty = ontModel.getDatatypeProperty(dataPropertyURI);
            if (indRes != null && datatypeProperty != null) {
                ontModel.removeAll(indRes, datatypeProperty, (Literal)null);
            }
        } finally {
        	getOntModel().getBaseModel().notifyEvent(new IndividualUpdateEvent(getWebappDaoFactory().getUserURI(),false,individualURI));
            ontModel.leaveCriticalSection();
        }
    }

    public void deleteDataPropertyStatementsForIndividualByDataProperty(Individual individual, DataProperty dataProperty) {
    	this.deleteDataPropertyStatementsForIndividualByDataProperty(individual.getURI(), dataProperty.getURI());
    }

    public Collection<DataPropertyStatement> getDataPropertyStatementsForIndividualByDataPropertyURI(Individual entity,
            String datapropURI) {
    	Collection<DataPropertyStatement> edList = new ArrayList<DataPropertyStatement>();
    	if (entity.getURI() == null) {
			return edList;
		}
    	try {	
	    	getOntModel().enterCriticalSection(Lock.READ);
	        OntResource ontRes = (VitroVocabulary.PSEUDO_BNODE_NS.equals(entity.getNamespace())) 
	        		? (OntResource) getOntModel().createResource(new AnonId(entity.getLocalName())).as(OntResource.class)
	        		: getOntModel().getOntResource(entity.getURI());
	        if (ontRes == null) {
	        	return edList;
	        }
	        ClosableIterator stmtIt;
	        stmtIt = (datapropURI != null) ? ontRes.listProperties(getOntModel().getProperty(datapropURI)) : ontRes.listProperties();
	        try {
	            while (stmtIt.hasNext()) {
	                Statement st = (Statement) stmtIt.next();
	                if (st.getObject().isLiteral()) {
	                    DataPropertyStatement ed = new DataPropertyStatementImpl();
	                    Literal lit = (Literal)st.getObject();
	                    fillDataPropertyStatementWithJenaLiteral(ed, lit);
	                    ed.setIndividualURI(entity.getURI());
	                    ed.setDatapropURI(st.getPredicate().getURI());
	                    edList.add(ed);
	                }
	            }
	        } finally {
	            stmtIt.close();
	        }
    	} finally {
    		getOntModel().leaveCriticalSection();
    	}
        return edList;
    }

    @Deprecated
    public List getExistingQualifiers(String datapropURI) {
        // TODO Auto-generated method stub
        return null;
    }
    
    private int NO_LIMIT = -1;
    
    public List<DataPropertyStatement> getDataPropertyStatements(DataProperty dp) {
    	return getDataPropertyStatements(dp, NO_LIMIT, NO_LIMIT);
    }
    
    public List<DataPropertyStatement> getDataPropertyStatements(DataProperty dp, int startIndex, int endIndex) {
    	getOntModel().enterCriticalSection(Lock.READ);
    	List<DataPropertyStatement> dpss = new ArrayList<DataPropertyStatement>();
    	try {
    		Property prop = ResourceFactory.createProperty(dp.getURI());
    		ClosableIterator dpsIt = getOntModel().listStatements(null,prop,(Literal)null);
    		try {
    			int count = 0;
    			while ( (dpsIt.hasNext()) && ((endIndex<0) || (count<endIndex)) ) {
    				++count;
    				Statement stmt = (Statement) dpsIt.next();
    				if (startIndex<0 || startIndex<=count) {
    					Literal lit = (Literal) stmt.getObject();
	    				DataPropertyStatement dps = new DataPropertyStatementImpl();
	    				dps.setDatapropURI(dp.getURI());
	    				dps.setIndividualURI(stmt.getSubject().getURI());
	    				fillDataPropertyStatementWithJenaLiteral(dps,lit);
	    				dpss.add(dps);
    				}
    			}
    		} finally {
    			dpsIt.close();
    		}
    	} finally {
    		getOntModel().leaveCriticalSection()
;    	}
    	return dpss;
    }

    public int insertNewDataPropertyStatement(DataPropertyStatement dataPropertyStmt) {
    	return insertNewDataPropertyStatement(dataPropertyStmt, getOntModelSelector().getABoxModel());
    }

    public int insertNewDataPropertyStatement(DataPropertyStatement dataPropertyStmt, OntModel ontModel) {
        ontModel.enterCriticalSection(Lock.WRITE);
        getOntModel().getBaseModel().notifyEvent(new IndividualUpdateEvent(getWebappDaoFactory().getUserURI(),true,dataPropertyStmt.getIndividualURI()));
		DataProperty dp = getWebappDaoFactory().getDataPropertyDao().getDataPropertyByURI(dataPropertyStmt.getDatapropURI());
		if ( (dataPropertyStmt.getDatatypeURI() == null) && (dp != null) && (dp.getRangeDatatypeURI() != null) ) {
			dataPropertyStmt.setDatatypeURI(dp.getRangeDatatypeURI());
		}
        Property prop = ontModel.getProperty(dataPropertyStmt.getDatapropURI());
        try {
            Resource res = ontModel.getResource(dataPropertyStmt.getIndividualURI());
            Literal literal = jenaLiteralFromDataPropertyStatement(dataPropertyStmt,ontModel);
            if (res != null && prop != null && literal != null && dataPropertyStmt.getData().length()>0) {
                res.addProperty(prop, literal);
            }
        } finally {
        	getOntModel().getBaseModel().notifyEvent(new IndividualUpdateEvent(getWebappDaoFactory().getUserURI(),false,dataPropertyStmt.getIndividualURI()));
            ontModel.leaveCriticalSection();
        }
        return 0;
    }
    
    private DataPropertyStatement fillDataPropertyStatementWithJenaLiteral(DataPropertyStatement dataPropertyStatement, Literal l) {
    	dataPropertyStatement.setData(l.getLexicalForm());
        dataPropertyStatement.setDatatypeURI(l.getDatatypeURI());
        dataPropertyStatement.setLanguage(l.getLanguage());
        return dataPropertyStatement;
    }
    
    private Literal jenaLiteralFromDataPropertyStatement(DataPropertyStatement dataPropertyStatement, OntModel ontModel) {
    	Literal l = null;
        if ((dataPropertyStatement.getLanguage()) != null && (dataPropertyStatement.getLanguage().length()>0)) {
        	l = ontModel.createLiteral(dataPropertyStatement.getData(),dataPropertyStatement.getLanguage());
        } else if ((dataPropertyStatement.getDatatypeURI() != null) && (dataPropertyStatement.getDatatypeURI().length()>0)) {
        	l = ontModel.createTypedLiteral(dataPropertyStatement.getData(),TypeMapper.getInstance().getSafeTypeByName(dataPropertyStatement.getDatatypeURI()));
        } else {
        	l = ontModel.createLiteral(dataPropertyStatement.getData());
        } 
        return l;
    }

}
