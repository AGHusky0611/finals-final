package Server.PlayerSide;


/**
* Server/PlayerSide/LobbyListHelper.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from playerinterface.idl
* Saturday, May 31, 2025 1:32:22 PM SGT
*/

abstract public class LobbyListHelper
{
  private static String  _id = "IDL:Server/PlayerSide/LobbyList:1.0";

  public static void insert (org.omg.CORBA.Any a, Server.CommonObjects.LobbyInfo[] that)
  {
    org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
    a.type (type ());
    write (out, that);
    a.read_value (out.create_input_stream (), type ());
  }

  public static Server.CommonObjects.LobbyInfo[] extract (org.omg.CORBA.Any a)
  {
    return read (a.create_input_stream ());
  }

  private static org.omg.CORBA.TypeCode __typeCode = null;
  synchronized public static org.omg.CORBA.TypeCode type ()
  {
    if (__typeCode == null)
    {
      __typeCode = Server.CommonObjects.LobbyInfoHelper.type ();
      __typeCode = org.omg.CORBA.ORB.init ().create_sequence_tc (0, __typeCode);
      __typeCode = org.omg.CORBA.ORB.init ().create_alias_tc (Server.PlayerSide.LobbyListHelper.id (), "LobbyList", __typeCode);
    }
    return __typeCode;
  }

  public static String id ()
  {
    return _id;
  }

  public static Server.CommonObjects.LobbyInfo[] read (org.omg.CORBA.portable.InputStream istream)
  {
    Server.CommonObjects.LobbyInfo value[] = null;
    int _len0 = istream.read_long ();
    value = new Server.CommonObjects.LobbyInfo[_len0];
    for (int _o1 = 0;_o1 < value.length; ++_o1)
      value[_o1] = Server.CommonObjects.LobbyInfoHelper.read (istream);
    return value;
  }

  public static void write (org.omg.CORBA.portable.OutputStream ostream, Server.CommonObjects.LobbyInfo[] value)
  {
    ostream.write_long (value.length);
    for (int _i0 = 0;_i0 < value.length; ++_i0)
      Server.CommonObjects.LobbyInfoHelper.write (ostream, value[_i0]);
  }

}
