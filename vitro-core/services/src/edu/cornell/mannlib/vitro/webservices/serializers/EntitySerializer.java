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

package edu.cornell.mannlib.vitro.webservices.serializers;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import org.apache.axis.Constants;
import org.apache.axis.encoding.SerializationContext;
import org.apache.axis.encoding.Serializer;
import org.apache.axis.wsdl.fromJava.Types;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;

/**
 * WARNING: SUPER AWFUL, OBJECT ORIENTED NO-NO'S AHEAD
 *  -- !serializing an entity will change it! --
 * * This is used to by the axis webservices. see vitro/webservices/wsdd/VitroWs.wsdd 
 */
public class EntitySerializer implements Serializer {
    public static final QName myTypeQName = new QName("typeNS", "VitroEntity");

    public static final String ID_MBER = "id";
    public static final String NAME_MBER= "name";
    public static final String MONIKER_MBER= "moniker";
    public static final String VCLASS_MBER= "vClass";
    public static final String URL_MBER= "url";
    public static final String DESCRIPTION_MBER= "description";
    public static final String SUNRISE_MBER= "sunrise";
    public static final String SUNSET_MBER= "sunset";
    public static final String TIMEKEY_MBER= "timekey";
    //public static final String MODTIME_MBER= "modTime";
    public static final String IMAGEFILE_MBER= "imageFile";
    public static final String ANCHOR_MBER= "anchor";
    public static final String BLURB_MBER= "blurb";    
    public static final String STATUS_MBER= "status";
    public static final String PROPERTYLIST_MBER= "propertyList";
    public static final String LINKSLIST_MBER= "linksList";
    public static final String KEYWORDS_MBER="keywords";
    public static final String VCLASSID_MBER= "vClassId";

    /** this is not on the original Entity object */
    public static final String RELATED_ENTITIES_MBER ="relatedEntities";

    /**
     * Serialize an element named name, with the indicated attributes
     * and value.
     * @param name is the element name
     * @param attributes are the attributes...serialize is free to add more.
     * @param value is the value
     * @param context is the SerializationContext
     */
    public void serialize(QName name, Attributes attributes,
                          Object value, SerializationContext context)
        throws IOException
    {
        if (!(value instanceof Individual))
            throw new IOException("Can't serialize a " + value.getClass().getName() + " with a EntitySerializer.");
        Individual ent = (Individual)value;
        
        // jc55 causes problems with web services if populated
        ent.setHiddenFromDisplayBelowRoleLevel(null);
        ent.setProhibitedFromUpdateBelowRoleLevel(null);

        context.startElement(name, attributes);

        prepareForSerialization( ent );

        context.serialize(new QName("",  ID_MBER), null, ent.getURI());
        context.serialize(new QName("",  NAME_MBER), null, ent.getName());
        context.serialize(new QName("",  MONIKER_MBER), null, ent.getMoniker());
        context.serialize(new QName("",  VCLASS_MBER), null, ent.getVClass());
        context.serialize(new QName("",  URL_MBER), null, ent.getUrl());
        context.serialize(new QName("",  DESCRIPTION_MBER), null, ent.getDescription());
        context.serialize(new QName("",  SUNRISE_MBER), null, ent.getSunrise());
        context.serialize(new QName("",  SUNSET_MBER), null, ent.getSunset());
        context.serialize(new QName("",  TIMEKEY_MBER), null, ent.getTimekey());
        context.serialize(new QName("",  IMAGEFILE_MBER), null, ent.getMainImageUri());
        context.serialize(new QName("",  ANCHOR_MBER), null, ent.getAnchor());
        context.serialize(new QName("",  BLURB_MBER), null, ent.getBlurb());
        context.serialize(new QName("",  STATUS_MBER), null, ent.getStatus());
        context.serialize(new QName("",  LINKSLIST_MBER), null, ent.getLinksList());
        context.serialize(new QName("",  KEYWORDS_MBER), null, ent.getKeywords());
        context.serialize(new QName("",  VCLASSID_MBER), null, ent.getVClassURI());

        //after the minimize this should only have property->ents2ent with no Entity objs.
        context.serialize(new QName("",  PROPERTYLIST_MBER), null, ent.getObjectPropertyList());

        context.endElement();
    }

    public String getMechanismType() { return Constants.AXIS_SAX; }

    public Element writeSchema(Class javaType, Types types) throws Exception {
        return null;
    }

    /**
     * 1) reflects props and ents2ents domainside,
     * 2) sorts
     * 3) removed domain Entity object from all ents2ents
     *
     */
    private final void prepareForSerialization(final Individual ent){
        if( ent == null || ent.getObjectPropertyList() == null) return;

        ent.sortForDisplay();
        //ent.forceAllPropertiesDomainSide();

        Iterator it = ent.getObjectPropertyList().iterator();
        while(it.hasNext()){
            ObjectProperty prop = (ObjectProperty)it.next();
            prepareProperty(prop);
        }
    }

    /**
     * calls prepareEnts2Ents on each ents2ents of Property
     */
    private final void prepareProperty(final ObjectProperty prop){
        if( prop == null || prop.getObjectPropertyStatements() == null ) return;

        Iterator it = prop.getObjectPropertyStatements().iterator();
        while(it.hasNext()){
            prepareEnts2Ents((ObjectPropertyStatement)it.next());
        }
    }

    private final void prepareEnts2Ents(final ObjectPropertyStatement e2e ){
        if( e2e != null){
            e2e.setSubject(null);
            e2e.setProperty(null);

            //we don't want to serialize any deeper
            Individual objInd = e2e.getObject();
            objInd.setObjectPropertyStatements(Collections.EMPTY_LIST);
            objInd.setDataPropertyStatements(Collections.EMPTY_LIST);
            objInd.setPropertyList(Collections.EMPTY_LIST);
            objInd.setDatatypePropertyList(Collections.EMPTY_LIST);

        }
    }
}
