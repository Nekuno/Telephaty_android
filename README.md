# Especificaciones Telephaty Qnoow
## Introducción
Durante el desarrollo de la API y pequeña aplicación Telephaty cuyo objetivo
principal es el envío de mensajes de difusión y privados mediante comunicaciones vía
Bluetooth se han tenido que tomar diferentes decisiones en cuanto al desarrollo, que
son las que en este documento abordamos.

El primer punto que hemos abordado en este proyecto es la imposibilidad en
Android de realizar comunicaciones Broadcast mediante BLE debido a que en Android
solo se permite esto a través de dispositivos con la versión 5.0 de Android y la
especificación 4.1 de Bluetooth de la que aúno no se dispone de suficiente información
(a fecha de Diciembre de 2014 sólo nexus 6 y nexus 9) aspecto que si ha sido posible
de realizar en IOS.

Debido a esto nos decantamos por hacer uso del modo Just Works que funciona
bajo el protocolo SSP que permite emparejar dispositivos sin necesidad de contraseña
o interacción alguna del usuario originalmente creado para su uso con dispositivos que
no disponen de pantalla y/o teclado. Mediante este modo simulamos el broadcast
mediante el sistema de conexión/envío/desconexión de manera individual entre todos
los dispositivos cercanos. Para poder realizar este proceso el dispositivo deber tener
Bluetooth activado y ser visible por parte de los dispositivos cercanos, aspecto que se
realiza de manera automática cuando se inicia la aplicación con la aprobación del
usuario.

Otro aspecto importante a destacar es que este modo no utiliza ningún
mecanismo de seguridad, aspecto por el cual hemos decido implementar una
combiancion del protocolo ECDH (Diffie­Hellman sobre curvas elípticas) para generar
la clave de 128 bits con la que se cifrarán las comunicaciones y el algoritmo de cifrado
simétrico AES, elegido como estándar de cifrado por parte de los Estados Unidos.
Para el desarrollo del sistema de comunicaciones se ha establecido un formato
de mensaje especial dividido por campos que disponen de diferentes características
utilizadas en el sistema de comunicaciones. A continuación se puede observar el
formato de mensaje creado.

## Formato del mensaje.
 Mensaje = Cabecera de 50 caracteres del Mensaje + Mensaje cifrado


<TABLE BORDER=1>

<TR>
<TD>
<b>Tipo de Mensaje</b><br>
(1 carácter)
</TD>

<TD>
<b>[0,1,2]</b><br>
0: no usado<br>
1: Broadcast<br>
2: Directo
</TD>
</TR>

<TR>
<TD>
<b>Id Fecha</b><br>
(14 caracteres)
</TD>

<TD>
ddMMyyyyHHmmss
</TD>
</TR>

<TR>
<TD>
<b>Nº de Saltos</b><br>
(1 carácter)
</TD>

<TD>
<b>[1-9]</b><br>
Por defecto, que sean siempre 8 saltos
</TD>
</TR>

<TR>
<TD>
<b>MAC destinatario</b><br>
(16 caracteres)
</TD>

<TD>
Sólo si el Mensaje es de Tipo 2, sino, este campo no
aparecerá.
</TD>
</TR>

<TR>
<TD>
<b>MAC emisor</b><br>
(16 caracteres)
</TD>

<TD>
Para conocer quién ha enviado el mensaje
originalmente.
</TD>
</TR>


<TR>
<TD>
<b>Parte del Mensaje</b><br>
(2 caracteres)
</TD>

<TD>
<b>[01­99]</b><br>
Utilizado por si el mensaje es muy largo poder
trocearlo.
</TD>
</TR>

<TR>
<TD>
<b>Autodestrucción</b><br>
(3 caracteres)
</TD>

<TD>
<b>[000­999]</b><br>
Lo que se almacena en este área es el tiempo en
minutos en los que se va a destruir el mensaje. Por
defecto es 0, que implica que no se va a destruir el
mensaje nunca.
</TD>
</TR>

<TR>
<TD>
<b>Mensaje cifrado</b><br>
(caracteres ilimitados)
</TD>

<TD>
</TD>
</TR>

</TABLE>


La librería creada está siempre en escucha mediante el uso de un servicio del
sistema para así poder recibir, enviar y generar notificaciones.El servicio permanece en
escucha en todo momento y reenvía o no mensajes dependiendo de si el número de
saltos del mensaje recibido (tercer campo de la cabecera) es mayor que 1. En caso
que sea mayor que 1, se decrementa ese campo y se reenvía a los dispositivos
cercanos sin modificar el resto del mensaje. En caso de que el primer campo de la
cabecera del mensaje sea 2, el mensaje se reenviará solamente cuando se cumpla el
supuesto anteriormente comentado y además si el destinatario del mensaje no es
nuestro dispositivo. En caso de que el destinatario del mensaje sea nuestro dispositivo,
el mensaje ya no se reenviará.


Además de esto se hace uso de una pequeña base de datos local para
almacenar los mensajes enviados y recibidos para en caso de recibir el mismo mensaje
por dos vías diferentes poder filtrarlo y sólo mostrarlo y reenviarlo una única vez. Para
ello se hace uso del identificador del mensaje y el emisor original de manera que no
podrá coincidir la dupla para dos mensajes diferentes.


La librería creada dispone de funciones para comprobar que el Bluetooth del
dispositivo está encendido y visible, para enviar mensajes de tipo Broadcast y para
enviar mensajes directos.


Otro problema con el que nos encontramos durante el desarrollo fue la
imposibilidad de realizar comunicaciones entre dispositivos Android e IOS debido
principalmente a que IOS solamente permite utilizar unos pocos perfiles de Bluetooth
sin que estos dispongan de la certificación MFi (Made For iPhone). El perfil de puerto
serie, que es el utilizado para realizar comunicaciones vía Bluetooth en IOS) requiere
que el otro dispositivo tenga la certificación MFi, ya que incorpora un mecanismo de
autenticación que verifica si el otro dispositivo tenga también esta certificación. Debido
a esto, aunque la comunicación “física” entre IOS y Android puede conseguirse, el
proceso falla en el momento de la autenticación.


## Funcionalidades de la API

Antes de comentar el funcionamiento de la API creada en este proyecto es
necesario revisar y analizar los paquetes o módulos de trabajo que lo componen. Estos
módulos se han generado de forma fragmentada para poder tener una mejor
organización del código generado por funcionalidades, pudiendo de esta forma
exportar funcionalidades o modificarlas de una forma más sencilla.




Los paquetes de trabajo que encontramos en esta API son los siguientes:

  * <b>Base de datos</b> (com.qnoow.telephaty.bbdd).
  
En este paquete encontramos todas las funciones relacionadas con el
almacenamiento de los datos que se utilizan en la aplicación. Los datos que
almacenamos están relacionados con los mensajes que enviamos o recibimos.


Dentro de las clases que encontramos en este paquete tenemos
<b>BBDDMensajes.java</b>, para la inicialización de las dos tablas que utilizamos en este
proyecto.


Por otro lado cada tabla cuenta con un controlador, <b>ControllerMensajes.java</b> es
la case que nos permite manipular los datos relacionados con los mensajes que
enviamos o recibimos y <b>ControllerMensajesCollection.java</b> es la clase en la que nos
apoyamos para almacenar las relaciones de los identificadores de mensajes con la
mac del usuario que la generó. Se han separado las tablas para poder trabajar de
manera independiente la parte visual de la API, con la primera clase comentada, de la
lógica de envío y recepción de mensajes, la segunda clase nombrada.


En estas clases las funciones implementadas están relacionadas con la
inserción y búsqueda de los elementos, cada una dentro de su ámbito de trabajo.

  * <b>Seguridad</b> (com.qnoow.telephaty.security).

Este paquete de trabajo es el encargado de contener todas las funcionalidades
relacionadas con la seguridad de la API. Está formado por dos clases java, una
llamada <b>ECDH.java</b> en donde se generan las claves compartidas que se utilizarán para
cifrar las comunicaciones utilizadas en este proyecto, basándose en el protocolo de
intercambio de claves Elliptic Curve Diffie Hellman. La otra clase de este paquete es
<b>Support.java</b>, en donde podemos encontrar las funciones encargadas de cifrar y
descifrar los mensajes.


  * <b>Bluetooth</b> (com.qnoow.telephaty.Bluetooth).
  
  Este es el paquete de las funcionalidades más importantes de la aplicación,
debido a que el código del envío de los mensajes se encuentra aquí.


En la clase <b>ConnectThread.java</b> encontramos hilo encargado de establecer las
conexiones. Este hilo se ejecuta mientras escucha las conexiones entrantes y se
comporta como un cliente­servidor. Se ejecuta hasta que se acepte una conexión (o
hasta que se cancele la misma) en el que crea un socket de escucha (seguro o
inseguro).


La clase <b>AcceptThread.java</b> es similar a la anterior, pero con la diferencia de
que es el encargado de de aceptar en vez de establecer una conexión. Se comporta de
la misma forma que el hilo anterior, se ejecuta hasta que se acepte una conexión (o
hasta que se cancele).


Finalmente la clase <b>ConnectedThread.java</b> es en donde se instancia al hilo que
se ejecuta durante una conexión con un dispositivo remoto. Maneja todas las
transmisiones entrantes y salientes. La primera conexión que se realiza entre
dispositivos es la encargada de enviar las claves generadas, una vez el protocolo de
intercambio de claves se genera se deja el socket listo esperando a que se envíe un
mensaje o a la espera de recibir alguno, dependiendo del lado de la comunicación en la
que nos encontremos.


Si queremos reenviar un mensaje se genera la estructura del paquete, antes
explicada, en este punto previo al envío. De la misma forma todos los mensajes que
han sido recibidos se decodifican en esta clase también.


La clase que va a ser la encargada de realizar las llamadas para el
establecimiento de la conexión es la clase llamada <b>Bluetooth.java</b>. En esta clase
podemos encontrar funciones relacionadas con la disponibilidad de bluetooth en el
dispositivo, con la activación o no del mismo, y su visibilidad. Encontramos también el
BroadcastReceiver, que va a ser el encargado de manejar el inicio y finalización del
escaneo de los dispositivos.


Las funciones encargadas de la conexión en alto nivel se encuentran también en
esta clase desde el establecimiento de la conexión como el cierre, así como las
encargadas del envío sencillo de mensajes y envío por broadcast.


La clase <b>Connection.java</b> es la encargada de aislar las clases de más bajo nivel
de de la conexión bluetooth de la propia interfaz de la clase en la que se utilice esta
API. Se ha generado con el fin de que una persona que quiera reutilizar este código
pueda hacerlo de una forma más sencilla y aislada mediante la llamada a funciones
más sencillas e intuitivas que las de la clase Bluetooth.java.

La clase <b>CustomHandler.java</b> es la encargada de comunicar en cada instante
el estado en el que se encuentra el dispositivo, es decir, gracias a esta clase sabemos
si estamos a la escucha de un mensaje, generando y compartiendo las claves, enviado
un mensaje o simplemente a la espera de la realización de una acción. Es una clase
con apariencia sencilla pero intrínsecamente es fundamentalmente para la
coordinación y el perfecto funcionamiento de la API.


La clase <b>DeviceListActivity.java</b> contiene la actividad encargada de se
enumerar los dispositivos vinculados y dispositivos detectados en la zona después de
su descubrimiento.


La clase <b>Notifications.java</b> es la encargada de definir las notificaciones que se
le van a mostar al usuario cuando tenga la aplicación cerrada ejecutándose en
segundo plano. En esta clase podemos definir los parámetros relacionados como la
foto que queremos que se nos muestre o el mensaje.


En la clase <b>Utilities.java</b> vamos a encontrar todos los parámentros y constantes
relacionados con el funcionamiento de la aplicación, como la definición de los
diferentes estados que va a tener el CustomHandler, los UUID de la aplicación, la lista
de las MACs asociadas al dispositivo o los parámentros del mensaje que se está
procesando.


## Usos de la API

Antes de realizar ninguna acción en nuestro código debemos de instanciar una clase
del tipo CustomHandler para el correcto funcionamiento de la comunicación del estado
actual del dispositivo con la actividad en la que queramos establecer las conexiones.

''mHandler = new CustomHandler(this);''

Luego debemos inicializar los parámetros de nuestro bluetooth, mediante la clase
Connection.


''Connection.myBluetooth = new Bluetooth(this, mHandler);''

Lo siguiente es inicializar los parámetros que vamos a utilizar en nuestras conexiones


''Utilities.mainContext = this;
Connection.mainContext = this;
Connection.myBluetooth
.registerBroadcastReceiver(getApplicationContext(), Connection.myBluetooth
.setBroadcastReceiver(getApplicationContext(),
mArrayAdapter));
Connection.mAdapter = BluetoothAdapter.getDefaultAdapter();''

Y debemos inicializar las notificaciones, en caso de querer utilizarlas, y la base de
datos.


''Utilities.notificationManager = new Notifications( (NotificationManager)
getSystemService(NOTIFICATION_SERVICE), this);
Connection.BBDDmensajes = new ControllerMensajes(this);
''

Lo siguiente que podemos realizar es enviar mensajes privados, de difusión o
mensajes directos, de la forma:



''Connection.privates = true; // Mensajes Privados
Connection.sendDifussionPrivate(“Mensaje”, MAC);
Connection.sendDifussion(“Mensaje”); // Mensajes de difusión
Connection.sendMessage(“Mensaje”); // Mensajes directos (No se utilizan actualmente)''


Podemos también realizar acciones como cerrar las conexiones:



''Connection.stopBluetooth();
''

También podemos activar o desactivar nuestra visibilidad, llamando a las activities:


''Connection.enableDiscoverability();
Connection.disableDiscoverability();''
