package Server.CommonObjects;


/**
* Server/CommonObjects/StringSequenceHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from commoninterface.idl
* Friday, May 30, 2025 7:36:24 PM SGT
*/

public final class StringSequenceHolder implements org.omg.CORBA.portable.Streamable
{
  public String value[] = null;

  public StringSequenceHolder ()
  {
  }

  public StringSequenceHolder (String[] initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = Server.CommonObjects.StringSequenceHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    Server.CommonObjects.StringSequenceHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return Server.CommonObjects.StringSequenceHelper.type ();
  }

}
