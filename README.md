# .cvox - Losslessly Compressed Voxel File Format

.cvox is a RIFF-like file format used to store voxel models, it is based on the MagicaVoxel .vox file format (https://github.com/ephtracy/voxel-model) which is used by many applications.
.cvox is losslessly compressed to be much smaller, and allows any amount of colours to be used, compared to the .vox colour limit of 255.

To illustrate the compression achieved by .cvox I have included the MagicaVoxel example .vox files, which have a combined size of 2,842,706 bytes.
The equivalent .cvox files have a size of only 312,664 bytes.

For a technical specification of the .cvox file format please see "cvox v1.txt".
In this readme I will give a general outline of the ideas and improvements behind .cvox.
Some of the ideas of this file format were already thought of by Xless-Qu-dev in this github issue thread: https://github.com/ephtracy/voxel-model/issues/52

This repository also contains a runnable example of vox to cvox (and vice-versa) conversion written in Java.
This example currently only supports 1 to 1 conversion, meaning cvox models must have 255 or less colours to be convertable.
I may add colour quantisation to the example in the future.

## Compression
### Coordinates
#### .vox
.vox stores each voxel coordinate in 3 bytes (x, y, z).
```
bytes required = v * 3 | v = amount of voxels
```
#### .cvox
.cvox is able to store cubes of voxels with the same colour.
A cube is stored in 6 bytes (low x, low y, low z, high x, high y, high z).
Due to the low amount of colours allowed in a .vox file (255 maximum), cubes can encompass a lot of voxels.
For simple models with few colours, generally, nearly all voxels will be part of cubes.
Voxels that are not part of cubes are stored in a separate chunk.
```
bytes required = c * 6 + nc * 3 | c = amount of cubes
                                | nc = amount of voxels which are not part of cubes
```

### Colours
#### .vox
.vox stores a 256 RGBA colour palette in 1024 bytes (although it is optional to forego this and use an implicit default palette).
One of these colours is not used, meaning there is a limit of 255 colours.
.vox stores a byte for each voxel which indicates its colour index.
```
bytes required = p * 1024 + v | v = amount of voxels
                              | p = a palette is used ? 1 : 0
```
#### .cvox
.cvox stores colours and the amounts of voxels/cubes which have that colour.
Effectively these are instructions of the following form (example): interpret the first 3 cubes as red cubes, the next 7 cubes as green cubes ... etc
The colour is stored as RGBA (4 bytes) and 3 additional bytes are used to specify the amount.
To store unused colours in a palette, the amount can be 0.

In .cvox, storage is tied to the amount of colours used instead of the amount of voxels.
By definition: `amount of colours used <= amount of voxels`

Due to voxels and cubes being stored separately, we may need 2 colour mappings for each colour used, but this is the worst case scenario and will generally still be much more efficient than .vox.
```
bytes required = (cc + ncc) * 7 | cc = amount of colours used by cubes
                                | ncc = amount of colours used by voxels which are not part of cubes
```

A positive side effect is that any amount of colours can be used, but for compatibility and compression it may be useful to try and use 255 or less colours.

## Expressiveness
### Colours
#### .vox
.vox is limited to 255 colours

#### .cvox
.cvox has no colour limit and can have 0 (no colour mapping chunks) or more colours.
In the case of 0 colours, the colour shown should be #AAAAAAFF (light grey).

### Size
Both .vox and .cvox support large models (limited to around 2^32 in each direction) in a similar way.
Models are limited to 256 voxels in each direction (x, y, z) so that coordinates can be stored in a single byte.
Multiple models can be stored in a file and these models can have an offset, meaning they can be combined by an application to form a single large model.

## Simplicity
### .vox
A .vox file consists of a file format information part, chunks and subchunks.
### .cvox
A .cvox file consists of chunks only.
