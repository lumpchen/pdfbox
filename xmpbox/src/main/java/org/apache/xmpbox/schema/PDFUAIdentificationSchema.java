package org.apache.xmpbox.schema;

import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.type.AbstractField;
import org.apache.xmpbox.type.Cardinality;
import org.apache.xmpbox.type.IntegerType;
import org.apache.xmpbox.type.PropertyType;
import org.apache.xmpbox.type.StructuredType;
import org.apache.xmpbox.type.Types;

@StructuredType(preferedPrefix = "pdfuaid", namespace = "http://www.aiim.org/pdfua/ns/id/")
public class PDFUAIdentificationSchema extends XMPSchema 
{

    @PropertyType(type = Types.Integer, card = Cardinality.Simple)
    public static final String PART = "part";
    
    public PDFUAIdentificationSchema(XMPMetadata metadata)
    {
        super(metadata);
    }

    public PDFUAIdentificationSchema(XMPMetadata metadata, String prefix)
    {
        super(metadata, prefix);
    }
    
    public IntegerType getPartProperty()
    {
        AbstractField tmp = getProperty(PART);
        if (tmp instanceof IntegerType)
        {
            return (IntegerType) tmp;
        }
        return null;
    }

    public void setPartProperty(IntegerType part)
    {
        addProperty(part);
    }
    
    public void setPart(Integer value)
    {
        setPartValueWithInt(value);
    }
    
    public void setPartValueWithInt(int value)
    {
        IntegerType part = (IntegerType) instanciateSimple(PART, value);
        addProperty(part);
    }
    
    public void setPartValueWithString(String value)
    {
        IntegerType part = (IntegerType) instanciateSimple(PART, value);
        addProperty(part);
    }
}
