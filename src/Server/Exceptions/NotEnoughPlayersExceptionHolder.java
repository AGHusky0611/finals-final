package Server.Exceptions;

/**
* Server/Exceptions/NotEnoughPlayersExceptionHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from exceptions.idl
* Friday, May 30, 2025 6:42:18 PM SGT
*/

public final class NotEnoughPlayersExceptionHolder implements org.omg.CORBA.portable.Streamable
{
  public Server.Exceptions.NotEnoughPlayersException value = null;

  public NotEnoughPlayersExceptionHolder ()
  {
  }

  public NotEnoughPlayersExceptionHolder (Server.Exceptions.NotEnoughPlayersException initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = Server.Exceptions.NotEnoughPlayersExceptionHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    Server.Exceptions.NotEnoughPlayersExceptionHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return Server.Exceptions.NotEnoughPlayersExceptionHelper.type ();
  }

}
