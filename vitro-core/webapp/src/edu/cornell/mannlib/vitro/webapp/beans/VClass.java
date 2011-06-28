/*
Copyright (c) 2011, Cornell University
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

package edu.cornell.mannlib.vitro.webapp.beans;

import java.text.Collator;

import org.openrdf.model.impl.URIImpl;

/**
 * A Java class representing an ontology ("Vitro") class
 *
 * [kwg8-07/30/07]: comments
 * [kwg8-07/30/07]: comments, naming cleanup
 */
public class VClass extends BaseResourceBean implements Comparable<VClass>
{
	
    /**
     * What this VClass is called
     */
    protected String myName = null;
    public  String getName()              { return myName; }
    public  void   setName( String name ) { myName = name; }

    /**
     * An example member of this VClass
     */
    protected String myExample = null;
    public  String getExample()                 { return myExample; }
    public  void   setExample( String example ) { myExample = example; }

    /**
     * Information about the type of information expected of a member of this VClass
     */
    protected String myDescription = null;
    public  String getDescription()               { return myDescription; }
    public  void   setDescription( String descr ) { myDescription = descr; }

    protected String myShortDefinition = null;
    public  String getShortDef()            { return myShortDefinition; }
    public  void   setShortDef( String sd ) { myShortDefinition = sd; }

    // TODO: [kwg8-08/01/07] What is this for?  It seems an entity count is the number of entities of
    // this type in the database.  Is this the case?
    // [bjl23 2007-08-12] Yep.  A count of individuals in the class.
    protected int  myEntityCount = -1;
    
    // rjy7 Removing deprecation since currently we have no other means to get this value.
    // @Deprecated
    public  int  getEntityCount()         { return myEntityCount; }
    
    public  void setEntityCount( int ec ) { myEntityCount = ec; }

    protected Integer  displayLimit = null;
    public  int  getDisplayLimit()  { return (displayLimit == null ? -1 : displayLimit); }
    public  void setDisplayLimit(int displayLimit) { this.displayLimit = displayLimit; }

    protected String quickEditJsp = null;
    public  String getQuickEditJsp()                    { return quickEditJsp; }
    public  void   setQuickEditJsp(String quickEditJsp) { this.quickEditJsp = quickEditJsp; }

    protected Integer  displayRank = null;
    public  int  getDisplayRank()                { return (displayRank == null ? -1 : displayRank); }
    public  void setDisplayRank(int displayRank) { this.displayRank = displayRank; }

    protected String  groupURI = null;
    public  String  getGroupURI()            { return groupURI; }
    public  void    setGroupURI(String groupURI) { this.groupURI = groupURI; }

    protected VClassGroup group=null;
    public  VClassGroup getGroup()                { return group; }
    public  void        setGroup(VClassGroup vcg) { group = vcg;  }

    protected String customEntryForm = null;
    public  String getCustomEntryForm()         { return customEntryForm; }
    public  void   setCustomEntryForm(String s) { this.customEntryForm = s; }
    
    protected String customDisplayView = null;
    public  String getCustomDisplayView()         { return customDisplayView; }
    public  void   setCustomDisplayView(String s) { this.customDisplayView = s; }
    
    protected String customShortView = null;
    public  String getCustomShortView()         { return customShortView; }
    public  void   setCustomShortView(String s) { this.customShortView = s; }
    
    protected String customSearchView = null;
    public  String getCustomSearchView()         { return customSearchView; }
    public  void   setCustomSearchView(String s) { this.customSearchView = s; }    

    protected Float searchBoost = null;
    public Float getSearchBoost() { return searchBoost; }
    public void setSearchBoost( Float boost ){ searchBoost = boost;}
    
    /**
     * Default constructor
     */
    public VClass()
    {
        super();
    }

    /**
     * Constructs the VClass from a URI that has been separated into namespace and localName components.
     * @param namespace The name-space for the URI
     * @param localName The local name for this URI
     * @param vclassName The name of the VClass
     */
    public VClass( String namespace, String localName, String vclassName )
    {
        myName = vclassName;
        this.namespace = namespace;
        this.localName = localName;
        URI = namespace + localName;
    }

    /**
     * Constructs the VClass with a given URI
     *   @param uriString The source string with which to create this URI
     */
    public VClass( String uriString )
    {
        // The URIImpl class can be used to parse a URI string into its component parts
        URIImpl uri = new URIImpl(uriString);

        // Use the URIImpl to obtain parts of this URI for local storage
        myName = uri.getLocalName();
        URI = uriString;
        namespace = uri.getNamespace();
        localName = uri.getLocalName();
    }
    
    /**
     * Sorts alphabetically by name
     */
    public int compareTo (VClass o1) {
        Collator collator = Collator.getInstance();
        return collator.compare(this.getName(),o1.getName());
    }

    /**
     * Converts the VClass to a string
     */
    public String toString()
    {
        // Get the name of this VClass
        String n = getName();

        // Set up a default name if none exists already
        if( n == null ) n = "null Name";

        // Build the return string
        return n + '(' + getURI() + ')';
    }
}
