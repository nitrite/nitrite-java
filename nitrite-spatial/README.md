# Nitrite Spatial Data

Nitrite Spatial is an extension of Nitrite database to support spatial data and spatial queries. 
It uses [JTS Topology Suite](https://github.com/locationtech/jts) as the underlying spatial library.

## How To Install

To use this add required dependencies:

**Maven**

```xml
<dependencies>
    <dependency>
        <groupId>org.dizitart</groupId>
        <artifactId>nitrite-spatial</artifactId>
    </dependency>
</dependencies>

```

**Gradle**

```groovy
implementation 'org.dizitart:nitrite-spatial'

```

## How To Use

### Initialize Spatial Module

```java
Nitrite db = Nitrite.builder()
        .loadModule(new JacksonMapperModule(new GeometryModule()))
        .loadModule(new SpatialModule())
        .openOrCreate();
```

### Create Spatial Index

Spatial index can be created on a field of type `Geometry` using annotation. 

```java
@Data
@Index(fields = "geometry", type = SPATIAL_INDEX)
public class SpatialData {
    @Id
    private Long id;
    private Geometry geometry;
}
```

It can also be created programmatically.

```java
collection.createIndex(IndexOptions.indexOptions(SPATIAL_INDEX), "location");
```

### Query Spatial Data

There are 3 types of spatial filter available in Nitrite Spatial.

#### Intersect Filter

A spatial filter which matches documents where the spatial data of a field intersects the specified `Geometry` value.

```java
WKTReader reader = new WKTReader();
Geometry search = reader.read("POLYGON ((490 490, 536 490, 536 515, 490 515, 490 490))");

Cursor<SpatialData> cursor = repository.find(where("geometry").intersects(search));
```

#### Within Filter

A spatial filter which matches documents where the spatial data of a field is within the specified `Geometry` value.

```java
WKTReader reader = new WKTReader();
Geometry search = reader.read("POLYGON ((490 490, 536 490, 536 515, 490 515, 490 490))");

Cursor<SpatialData> cursor = repository.find(where("geometry").within(search));
```

#### Near Filter

A spatial filter which matches documents where the spatial data of a field is near the specified coordinate within a distance.

```java
WKTReader reader = new WKTReader();
Point search = (Point) reader.read("POINT (490 490)");

Cursor<SpatialData> cursor = repository.find(where("geometry").near(search, 20.0));
```