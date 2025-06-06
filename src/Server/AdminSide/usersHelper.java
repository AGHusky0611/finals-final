package Server.AdminSide;


/**
* Server/AdminSide/usersHelper.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from admininterface.idl
* Friday, May 30, 2025 8:51:05 PM SGT
*/

abstract public class usersHelper
{
  private static String  _id = "IDL:Server/AdminSide/users:1.0";

  public static void insert (org.omg.CORBA.Any a, Server.CommonObjects.User[] that)
  {
    org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
    a.type (type ());
    write (out, that);
    a.read_value (out.create_input_stream (), type ());
  }

  public static Server.CommonObjects.User[] extract (org.omg.CORBA.Any a)
  {
    return read (a.create_input_stream ());
  }

  private static org.omg.CORBA.TypeCode __typeCode = null;
  synchronized public static org.omg.CORBA.TypeCode type ()
  {
    if (__typeCode == null)
    {
      __typeCode = Server.CommonObjects.UserHelper.type ();
      __typeCode = org.omg.CORBA.ORB.init ().create_sequence_tc (0, __typeCode);
      __typeCode = org.omg.CORBA.ORB.init ().create_alias_tc (Server.AdminSide.usersHelper.id (), "users", __typeCode);
    }
    return __typeCode;
  }

  public static String id ()
  {
    return _id;
  }

  public static Server.CommonObjects.User[] read (org.omg.CORBA.portable.InputStream istream)
  {
    Server.CommonObjects.User value[] = null;
    int _len0 = istream.read_long ();
    value = new Server.CommonObjects.User[_len0];
    for (int _o1 = 0;_o1 < value.length; ++_o1)
      value[_o1] = Server.CommonObjects.UserHelper.read (istream);
    return value;
  }

  public static void write (org.omg.CORBA.portable.OutputStream ostream, Server.CommonObjects.User[] value)
  {
    ostream.write_long (value.length);
    for (int _i0 = 0;_i0 < value.length; ++_i0)
      Server.CommonObjects.UserHelper.write (ostream, value[_i0]);
  }

}
