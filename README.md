# Phase Congruency
El presente repositorio contiene la implementación de 
la Congruencia de fase (Phase Congruency) en la plataforma ImageJ en 
su versión 1.52e. Esta versión se basa en parte de la propuesta realizada 
por Peter Kovesi en su [código](https://www.peterkovesi.com/matlabfns/PhaseCongruency/phasecongmono.m) disponible en su [página](https://www.peterkovesi.com/matlabfns/index.html), 
agregándole aportes tras el estudio de esta técnica, por lo cual para 
información adicional se puede revisar:

* Jacanamejoy, C. A., & Forero, M. G. (2018, November). A Note on the Phase
  Congruence Method in Image Analysis. In Iberoamerican Congress on Pattern
  Recognition (pp. 384-391). Springer, Cham.[link](https://link.springer.com/chapter/10.1007/978-3-030-13469-3_45)

* Jamioy, C. J., Meneses-Casas, N., & Forero, M. G. (2019, July). Image Feature Detection Based on Phase Congruency by Monogenic Filters with New Noise Estimation. In Iberian Conference on Pattern Recognition and Image Analysis (pp. 577-588). Springer, Cham. [link](https://link.springer.com/chapter/10.1007/978-3-030-31332-6_50)


## Instalación
Para instalar el plugin se puede revisar en la ayuda de [ImageJ](https://imagejdocu.tudor.lu/howto/plugins/how_to_install_a_plugin)

## Parámetros del plugin

![interfaz](https://github.com/carloskl12/phase_Congruency/blob/master/menusPC.png)

Consta de tres tipos de entradas, primero los parámetros básicos 
indicando entre paréntesis su valor por defecto:
 - **Scales** (4), hace referencia al 
  número de escalas utilizadas.

 - **Lenght edge** (3), corresponde a la ongitud en pixeles de la escala más
  pequeña a tener en cuenta.

 - **Mult** (2.1), factor de escala entre los filtros sucesivos.

 - **Sigma Onf** ( 0.55), define la forma de los filtros log Gabor, afectando
  directamente el ancho de banda de los mismos.

 - **k** ( 3) , factor de la raíz cuadrada de la varianza de la distribución Rayleigh que 
  modela el ruido.

 - **Cut Off** (0.5), punto de corte para penalizar la PC. Un valor de 0 implica no
  penalizar nada, y 1 corresponde a la máxima penalización debido a la
  distribución de frecuencias de la imágen.

 - **Gain** (10), ganancia que implica un aumento del contraste
  de la imagen resultante.

 - **Alpha** (1.5), sensibilidad lineal ante los cambios de fase. Un valor alto significa que 
  se detectarán valores de congruencia de fase más altos, y se despreciará los 
  más bajos.

Luego en el parámetro **Threshold** se escoge el método de estimación del umbral
de ruido, que en caso de requerir algún valor adicional se ingresa en el campo
**Value**. Y finalmente se indican las imágenes de salida a mostrar:

 - **PC**, imagen de congruencia de fase.
 
 - **F**, imagen sin la componente DC.
 
 - **H**, imagen con desfase de 90 grados, o componente imaginaria.
 
 - **E**, imagen de energía local.

 - **Ph**, imagen de fase.
 
 - **Or**, imagen de la orientación o magnitud del gradiente de la fase.


