drop keyspace Annuaire;

create keyspace Annuaire 
with placement_strategy = 'SimpleStrategy'
and strategy_options= {replication_factor:1};

use Annuaire;

create column family User
  with comparator = 'UTF8Type'
  and default_validation_class = 'BytesType'
  and key_validation_class = 'UTF8Type'
  and column_metadata = [
    {column_name : 'first',
    validation_class : UTF8Type},
    {column_name : 'last',
    validation_class : UTF8Type},
    {column_name : 'age',
    validation_class : UTF8Type}];

set User['user1']['last']='Lefort';
set User['user1']['first']='Cedric';
set User['user1']['age']=34;
